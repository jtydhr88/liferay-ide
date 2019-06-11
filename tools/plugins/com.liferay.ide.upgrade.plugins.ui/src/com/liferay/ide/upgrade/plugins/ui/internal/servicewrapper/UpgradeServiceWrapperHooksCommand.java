/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.upgrade.plugins.ui.internal.servicewrapper;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.gradle.core.GradleUtil;
import com.liferay.ide.project.core.model.ProjectName;
import com.liferay.ide.project.core.model.ProjectNamedItem;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.PropertyKey;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.ui.util.UIUtil;
import com.liferay.ide.upgrade.plan.core.MessagePrompt;
import com.liferay.ide.upgrade.plan.core.UpgradeCommand;
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plugins.core.NewBasicLiferayModuleProjectOp;
import com.liferay.ide.upgrade.plugins.core.UpgradeServiceWrapperHooksOp;
import com.liferay.ide.upgrade.plugins.ui.internal.UpgradePluginsUIPlugin;
import com.liferay.ide.upgrade.plugins.ui.serivcewrapper.UpgradeServiceWrapperHooksCommandKeys;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.w3c.dom.NodeList;

/**
 * @author Seiphon Wang
 */
@Component(
	property = "id=" + UpgradeServiceWrapperHooksCommandKeys.ID, scope = ServiceScope.PROTOTYPE,
	service = UpgradeCommand.class
)
@SuppressWarnings("restriction")
public class UpgradeServiceWrapperHooksCommand implements SapphireContentAccessor, UpgradeCommand {

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		//		NewLiferayModuleProjectOp newLiferayModuleProjectOp = NewLiferayModuleProjectOp.TYPE.instantiate();

		UpgradePlan upgradePlan = _upgradePlanner.getCurrentUpgradePlan();

		Map<String, String> upgradeContext = upgradePlan.getUpgradeContext();

		String currentProjectLocation = upgradeContext.get("currentProjectLocation");

		if (currentProjectLocation == null) {
			return UpgradePluginsUIPlugin.createErrorStatus("There is no current project configured for current plan.");
		}

		if (FileUtil.notExists(new File(currentProjectLocation))) {
			return UpgradePluginsUIPlugin.createErrorStatus("There is no code located at " + currentProjectLocation);
		}

		String targetProjectLocation = upgradeContext.get("targetProjectLocation");

		if (targetProjectLocation == null) {
			return UpgradePluginsUIPlugin.createErrorStatus("There is no target project configured for current plan.");
		}

		if (FileUtil.notExists(new File(targetProjectLocation))) {
			return UpgradePluginsUIPlugin.createErrorStatus("There is no code located at " + targetProjectLocation);
		}

		final AtomicInteger returnCode = new AtomicInteger();

		UpgradeServiceWrapperHooksOp sdkHookProjectsSelectOp = UpgradeServiceWrapperHooksOp.TYPE.instantiate();

		UIUtil.sync(
			() -> {
				UpgradeServiceWrapperHooksWizard importSDKProjectsWizard = new UpgradeServiceWrapperHooksWizard(
					sdkHookProjectsSelectOp, Paths.get(currentProjectLocation));

				IWorkbench workbench = PlatformUI.getWorkbench();

				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				Shell shell = workbenchWindow.getShell();

				WizardDialog wizardDialog = new WizardDialog(shell, importSDKProjectsWizard);

				returnCode.set(wizardDialog.open());
			});

		IStatus status = Status.OK_STATUS;

		if (returnCode.get() == Window.OK) {
			ElementList<ProjectNamedItem> projects = sdkHookProjectsSelectOp.getSelectedProjects();

			//			Stream<ProjectNamedItem> stream = projects.stream();
			//
			//			stream.map(
			//				projectNamedItem -> get(projectNamedItem.getLocation())
			//			).forEach(
			//				location -> {
			//
			//				}
			//			);

			for (ProjectNamedItem project : projects) {
				String location = get(project.getLocation());

				File file = new File(location + "/docroot/WEB-INF/liferay-hook.xml");

				if (FileUtil.exists(file)) {
					status = _doWithLiferayHookXML(file, progressMonitor);
				}
			}
		}

		return status;
	}

	@SuppressWarnings("deprecation")
	private IStatus _copyClassContent(IProgressMonitor monitor, File oldFile, File newFile) {
		IStatus retval = Status.OK_STATUS;

		try {
			String oldReadContents = FileUtil.readContents(oldFile, true);
			String newReadContents = FileUtil.readContents(newFile, true);

			ASTParser oldFileastParser = ASTParser.newParser(AST.JLS8);
			ASTParser newFileastParser = ASTParser.newParser(AST.JLS8);

			oldFileastParser.setSource(oldReadContents.toCharArray());
			newFileastParser.setSource(newReadContents.toCharArray());

			oldFileastParser.setKind(ASTParser.K_COMPILATION_UNIT);
			newFileastParser.setKind(ASTParser.K_COMPILATION_UNIT);

			CompilationUnit oldFileCU = (CompilationUnit)oldFileastParser.createAST(monitor);
			CompilationUnit newFileCU = (CompilationUnit)newFileastParser.createAST(monitor);

			Document document = new Document(new String(newReadContents));

			newFileCU.recordModifications();

			//List<ImportDeclaration> importDeclarations = (List<ImportDeclaration>)oldFileCU.imports();
			TypeDeclaration typeDeclaration = (TypeDeclaration)oldFileCU.types().get(0);

			ASTNode note = typeDeclaration.copySubtree(newFileCU.getAST(), typeDeclaration);

			newFileCU.types().remove(0);
			newFileCU.types().add(note);

			ASTRewrite rewrite = ASTRewrite.create(newFileCU.getAST());

			ListRewrite newLrw = rewrite.getListRewrite(newFileCU, CompilationUnit.TYPES_PROPERTY);

			newLrw.insertAt(typeDeclaration, 0, null);

			try (OutputStream fos = Files.newOutputStream(newFile.toPath())) {
				TextEdit edits = rewrite.rewriteAST(document, null);

				edits.apply(document);

				String content = document.get();

				fos.write(content.getBytes());

				fos.flush();
			}

			//			oldFileCU.accept(
			//				new ASTVisitor() {

			//
			//					@Override
			//					public boolean visit(TypeDeclaration node) {
			//						_classNode.add(node);
			//
			//						node.copySubtree(newFileCU.getAST(), node);
			//
			//						return super.visit(node);
			//					}
			//
			//					@Override
			//					public boolean visit(ImportDeclaration node) {
			//						_importDeclaration.add(node);
			//
			//						return super.visit(node);
			//					}
			//				});

		}
		catch (Exception e) {
			retval = UpgradePluginsUIPlugin.createErrorStatus("Can not create module project: " + e.getMessage(), e);
		}

		return retval;
	}

	private IStatus _creatNewModuleProject(
		IProgressMonitor monitor, ArrayList<String> serviceTypeContents, ArrayList<String> serviceImplContents) {

		IStatus retval = Status.CANCEL_STATUS;

		NewBasicLiferayModuleProjectOp newBasicLiferayModuleProjectOp =
			NewBasicLiferayModuleProjectOp.TYPE.instantiate();

		UpgradePlan upgradePlan = _upgradePlanner.getCurrentUpgradePlan();

		Map<String, String> upgradeContext = upgradePlan.getUpgradeContext();

		String targetProjectLocation = upgradeContext.get("targetProjectLocation");

		newBasicLiferayModuleProjectOp.setLiferayVersion(upgradePlan.getTargetVersion());

		String defaultProjectName = serviceImplContents.get(0);

		newBasicLiferayModuleProjectOp.setComponentName(
			defaultProjectName.substring(defaultProjectName.lastIndexOf(".") + 1));

		newBasicLiferayModuleProjectOp.setPackageName(
			defaultProjectName.substring(0, defaultProjectName.lastIndexOf(".")));

		newBasicLiferayModuleProjectOp.setServiceName(serviceTypeContents.get(0));

		final AtomicInteger returnCode = new AtomicInteger();

		UIUtil.sync(
			() -> {
				NewBasicLiferayModuleProjectWizard newBasicLiferayModuleProjectWizard =
					new NewBasicLiferayModuleProjectWizard(
						newBasicLiferayModuleProjectOp, Paths.get(targetProjectLocation));

				IWorkbench workbench = PlatformUI.getWorkbench();

				IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

				Shell shell = workbenchWindow.getShell();

				WizardDialog wizardDialog = new WizardDialog(shell, newBasicLiferayModuleProjectWizard);

				returnCode.set(wizardDialog.open());
			});

		if (returnCode.get() == Window.OK) {
			String projectName = get(newBasicLiferayModuleProjectOp.getProjectName());

			IPath location = PathBridge.create(get(newBasicLiferayModuleProjectOp.getLocation()));

			String className = get(newBasicLiferayModuleProjectOp.getComponentName());

			String liferayVersion = get(newBasicLiferayModuleProjectOp.getLiferayVersion());

			String serviceName = get(newBasicLiferayModuleProjectOp.getServiceName());

			String packageName = get(newBasicLiferayModuleProjectOp.getPackageName());

			ElementList<PropertyKey> propertyKeys = newBasicLiferayModuleProjectOp.getPropertyKeys();

			List<String> properties = new ArrayList<>();

			for (PropertyKey propertyKey : propertyKeys) {
				properties.add(get(propertyKey.getName()) + "=" + get(propertyKey.getValue()));
			}

			File targetDir = location.toFile();

			String projectTemplateName = get(newBasicLiferayModuleProjectOp.getProjectTemplateName());

			StringBuilder sb = new StringBuilder();

			sb.append("create ");
			sb.append("-d \"");
			sb.append(targetDir.getAbsolutePath());
			sb.append("\" ");

			IProject liferayWorkspaceProject = LiferayWorkspaceUtil.getWorkspaceProject();

			if (liferayWorkspaceProject != null) {
				sb.append("--base \"");

				IPath workspaceLocation = liferayWorkspaceProject.getLocation();

				sb.append(workspaceLocation.toOSString());

				sb.append("\" ");
			}

			sb.append("-v ");
			sb.append(liferayVersion);
			sb.append(" ");
			sb.append("-t ");
			sb.append(projectTemplateName);
			sb.append(" ");

			if (className != null) {
				sb.append("-c ");
				sb.append(className);
				sb.append(" ");
			}

			if (serviceName != null) {
				sb.append("-s ");
				sb.append(serviceName);
				sb.append(" ");
			}

			if (packageName != null) {
				sb.append("-p ");
				sb.append(packageName);
				sb.append(" ");
			}

			sb.append("\"");
			sb.append(projectName);
			sb.append("\" ");

			try {
				BladeCLI.execute(sb.toString());

				ElementList<ProjectName> projectNames = newBasicLiferayModuleProjectOp.getProjectNames();

				ProjectName name = projectNames.insert();

				name.setName(projectName);

				if (projectTemplateName.equals("service-builder")) {
					name = projectNames.insert();

					name.setName(projectName + "-api");

					name = projectNames.insert();

					name.setName(projectName + "-service");
				}

				IPath projectLocation = location;

				String lastSegment = location.lastSegment();

				if ((location != null) && (location.segmentCount() > 0)) {
					if (!lastSegment.equals(projectName)) {
						projectLocation = location.append(projectName);
					}
				}

				_newProjectLocation = projectLocation;
				_newPakageName = packageName;
				_newComponentClassName = className;

				boolean hasGradleWorkspace = LiferayWorkspaceUtil.hasGradleWorkspace();
				boolean useDefaultLocation = get(newBasicLiferayModuleProjectOp.getUseDefaultLocation());
				boolean inWorkspacePath = false;

				if (hasGradleWorkspace && (liferayWorkspaceProject != null) && !useDefaultLocation) {
					IPath workspaceLocation = liferayWorkspaceProject.getLocation();

					if (workspaceLocation != null) {
						inWorkspacePath = workspaceLocation.isPrefixOf(projectLocation);
					}
				}

				if ((hasGradleWorkspace && useDefaultLocation) || inWorkspacePath) {
					GradleUtil.refreshProject(liferayWorkspaceProject);
				}
				else {
					CoreUtil.openProject(projectName, projectLocation, monitor);

					GradleUtil.synchronizeProject(projectLocation, monitor);
				}

				retval = Status.OK_STATUS;
			}
			catch (Exception e) {
				retval = UpgradePluginsUIPlugin.createErrorStatus(
					"Can not create module project: " + e.getMessage(), e);
			}
		}

		return retval;
	}

	@SuppressWarnings("deprecation")
	private IStatus _doWithLiferayHookXML(File file, IProgressMonitor monitor) {
		IStatus retval = Status.OK_STATUS;

		IDOMModel domModel = null;

		try (InputStream input = Files.newInputStream(Paths.get(file.toURI()), StandardOpenOption.READ)) {
			IModelManager modelManager = StructuredModelManager.getModelManager();

			domModel = (IDOMModel)modelManager.getModelForRead(file.getAbsolutePath(), input, null);

			IDOMDocument document = domModel.getDocument();

			NodeList serviceElements = document.getElementsByTagName("service");

			ArrayList<String> serviceTypeContents = new ArrayList<>();
			ArrayList<String> serviceImplContents = new ArrayList<>();

			if (serviceElements != null) {
				for (int i = 0; i < serviceElements.getLength(); i++) {
					NodeList serviceTypeElements = document.getElementsByTagName("service-type");

					IDOMElement serviceTypeElement = (IDOMElement)serviceTypeElements.item(0);

					serviceTypeContents.add(serviceTypeElement.getTextContent());

					NodeList serviceImplElements = document.getElementsByTagName("service-impl");

					IDOMElement serviceImpleElement = (IDOMElement)serviceImplElements.item(0);

					serviceImplContents.add(serviceImpleElement.getTextContent());
				}

				retval = _creatNewModuleProject(monitor, serviceTypeContents, serviceImplContents);
			}

			if (retval == Status.OK_STATUS) {
				String location = file.getParent();

				String oldFilePath = serviceImplContents.get(0);

				oldFilePath = oldFilePath.replace(".", "/");

				File oldFile = new File(location + "/src/" + oldFilePath + ".java");

				String pakageName = _newPakageName.replace(".", "/");

				File newFile = new File(
					_newProjectLocation.toString() + "/src/main/java/" + pakageName + "/" + _newComponentClassName +
						".java");

				if (FileUtil.exists(oldFile) && FileUtil.exists(newFile)) {
					retval = _copyClassContent(monitor, oldFile, newFile);
				}
			}
		}
		catch (Exception e) {
			retval = UpgradePluginsUIPlugin.createErrorStatus("Can not create module project: " + e.getMessage(), e);
		}
		finally {
			if (domModel != null) {
				domModel.releaseFromRead();
			}
		}

		return retval;
	}

	@Reference
	private MessagePrompt _messagePrompt;

	private String _newComponentClassName;
	private String _newPakageName;
	private IPath _newProjectLocation;

	@Reference
	private UpgradePlanner _upgradePlanner;

}