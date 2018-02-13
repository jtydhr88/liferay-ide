
package com.liferay.ide.maven.ui.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.maven.core.ILiferayMavenConstants;
import com.liferay.ide.maven.core.MavenUtil;
import com.liferay.ide.maven.core.aether.AetherUtil;
import com.liferay.ide.maven.ui.LiferayMavenUI;
import com.liferay.ide.maven.ui.MavenUIProjectBuilder;
import com.liferay.ide.project.core.util.SearchFilesVisitor;

public class LiferayMavenDynamicMenuCreator extends ContributionItem {

	public LiferayMavenDynamicMenuCreator() {
	}
	
	protected ImageDescriptor getDefaultImageDescriptor() {
		return LiferayMavenUI.imageDescriptorFromPlugin(LiferayMavenUI.PLUGIN_ID, "/icons/m2e-liferay.png");
	}
	
	private void addGoalActionMenu(List<LifeayDynamicPopupMenu> popupMenuList) {
		int index = 0;
		for( int i=0;i<popupMenuList.size();i++) {
			LifeayDynamicPopupMenu popupMenu = popupMenuList.get(i);

			Set<String> goals = popupMenu.getGoals();
			
			String goalPrefix = popupMenu.getGoalPrefix();
			String[] goalArray = goals.toArray(new String[goals.size()]);

			for(int j=0;j<goalArray.length;j++) {
				String goal = goalArray[j];
				MenuItem menuItem = new MenuItem(popupMenu.getParentMenu(), SWT.NONE, index);
				menuItem.setText(goalPrefix+":"+goal);
				menuItem.setImage(getDefaultImageDescriptor().createImage());
				menuItem.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {
						runMavenGoalPostAction(popupMenu.getProject(),setGoal(goalPrefix,goal));
					}
				});
				index++;
			}
		}
	}

	private String setGoal(String goalPrefix, String goal) {
		if ( goalPrefix != null) {
			return goalPrefix+":"+goal;
		}
		else {
			return goal;
		}
	}

	private void runMavenGoalPostAction( IProject project, String goal ) {
		Job job = new Job(project.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(goal, 100);
					IFile pomFile = project.getFile("pom.xml");
					_runMavenGoal(pomFile, goal, monitor);
							
					updateProject(project, monitor);

				}
				catch( Exception e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS; 
			}

		};
		job.schedule();
	}
	
	private IProject getParentProject(IProject checkProject) {
		try {
			IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(checkProject, new NullProgressMonitor());
			MavenProject parent = projectFacade.getMavenProject(new NullProgressMonitor() ).getParent();
			return parent!=null?CoreUtil.getProject(parent.getName()):null;
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<IProject> getServiceParentProject(IProject checkProject) {
		List<IProject> parentProjects= new ArrayList<IProject>();
		if (checkProject != null) {
			List<IFile> serviceXmlFiles = new MavenSearchFilesVisitor().searchFiles(
				checkProject, "service.xml");

			if (serviceXmlFiles.size() == 1 ) {
				IPath servicePath = serviceXmlFiles.get(0).getFullPath();
				int segmentCounts = servicePath.segmentCount();
				IProject serviceProject = CoreUtil.getProject(servicePath.segment(segmentCounts-2));
				
				if ((segmentCounts == 2) && checkProject.equals(serviceProject)) {
					IProject parentProject = getParentProject(serviceProject);
					if (parentProject!=null) {
						parentProjects.add(parentProject);						
					}
					return parentProjects;
				}
				else {
					if (segmentCounts > 3) {
						IPath  prefixPath = servicePath.removeLastSegments(3);
						servicePath = servicePath.makeRelativeTo(prefixPath);
					}

					if (servicePath.segmentCount() == 3) {
						IPath serviceProjectLocation = servicePath.removeLastSegments(1);
						String paretnProjectName = serviceProjectLocation.segment(serviceProjectLocation.segmentCount()-2);
						IProject sbProject = CoreUtil.getProject(paretnProjectName);
						if ( sbProject != null) {
							parentProjects.add(sbProject);
							return parentProjects;
						}
					}
				}
			}
			else if (serviceXmlFiles.size() > 1) {
				serviceXmlFiles.stream().forEach( _serviceXmlPath -> {
					IPath servicePath = _serviceXmlPath.getFullPath();
					int segmentCounts = servicePath.segmentCount();

					if (segmentCounts > 3) {
						IPath  prefixPath = servicePath.removeLastSegments(3);
						servicePath = servicePath.makeRelativeTo(prefixPath);
					}

					if (servicePath.segmentCount() == 3) {
						IPath serviceProjectLocation = servicePath.removeLastSegments(1);
						String paretnProjectName = serviceProjectLocation.segment(serviceProjectLocation.segmentCount()-2);
						IProject sbProject = CoreUtil.getProject(paretnProjectName);

						if ( sbProject.exists()) {
							parentProjects.addAll(getServiceParentProject(sbProject));
						}
					}
				});
				return parentProjects;
			}
			else if (serviceXmlFiles.size() == 0) {
				return getServiceParentProject(getParentProject(checkProject));
			}
		}
		else {
			return parentProjects;
		}

		return parentProjects;
	}	
	
	private void updateProject(IProject project, IProgressMonitor monitor) {
		
		List<IFile> pomXmlFiles = new MavenSearchFilesVisitor().searchFiles(project, "service.xml");

		if ( ListUtil.isNotEmpty(pomXmlFiles)) {
			List<IProject> serviceParentProject = getServiceParentProject(project);
			IProject parentProject = getParentProject(project);
			
			if (parentProject!=null && serviceParentProject.contains(parentProject)) {
				Stream<IProject> stream = serviceParentProject.stream();
				stream.filter( filterProject -> filterProject.equals(parentProject)).
				forEach( _project -> {
					try {
						_project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}
					catch (CoreException ce) {
						LiferayMavenUI.logError("Error refreshing project", ce);
					}
				});
			}
			else {
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				catch (CoreException ce) {
					LiferayMavenUI.logError("Error refreshing project", ce);
				}
			}
		}
		else {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
			catch (CoreException ce) {
				LiferayMavenUI.logError("Error refreshing project", ce);
			}
		}
	}	
	
	private void _runMavenGoal(IFile pomFile, String goal, IProgressMonitor monitor) throws CoreException {
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();

		IMavenProjectFacade projectFacade = projectManager.create(pomFile, true, new NullProgressMonitor());
		MavenUIProjectBuilder builder = new MavenUIProjectBuilder(pomFile.getProject());

		builder.runMavenGoal(projectFacade, goal, "run", monitor);
	}

	@Override
	public void fill(final Menu menu, final int index) {
		IProject project = null;
		try {
			ISelection selection =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();

			if (selection instanceof TreeSelection) {
				TreeSelection s =(TreeSelection)selection;
				Object firstElement = s.getFirstElement();
				if ( !(firstElement instanceof IProject)) {
					return;					
				}
				project = (IProject) firstElement;
			}

			IMavenProjectFacade mavenProjectFacade = MavenUtil.getProjectFacade(project);
			MavenProject mavenProject = mavenProjectFacade.getMavenProject(new NullProgressMonitor());
			List<RemoteRepository> remotePluginRepositories = mavenProjectFacade.getMavenProject().getRemotePluginRepositories();
			List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
			List<LifeayDynamicPopupMenu> popupMenuList = new ArrayList<LifeayDynamicPopupMenu>();
			for(int i=0;i<buildPlugins.size();i++) {
				Plugin plugin = buildPlugins.get(i);

				if ( !plugin.getGroupId().equals(ILiferayMavenConstants.NEW_LIFERAY_MAVEN_PLUGINS_GROUP_ID) ) {
					continue;
				}

				PluginDescriptor pluginDescriptor = anlayzePlugin(plugin, remotePluginRepositories);
				
				LifeayDynamicPopupMenu popupMenu = new LifeayDynamicPopupMenu(project,pluginDescriptor.getGoalPrefix(), menu );
				
				List<PluginExecution> executions = plugin.getExecutions();
				for( int j=0;j<executions.size();j++) {
					PluginExecution pluginExecution = executions.get(j);
					List<String> goals = pluginExecution.getGoals();
					for( int k=0;k<goals.size();k++) {
						popupMenu.addGoal(goals.get(k));
					}
				}

				List<MojoDescriptor> mojos = pluginDescriptor.getMojos();
				for( int l=0;l<mojos.size();l++) {
					MojoDescriptor mojo = mojos.get(l);
					String executeGoal = mojo.getGoal();
					popupMenu.addGoal(executeGoal);
				}

				popupMenuList.add(popupMenu);
			}
			addGoalActionMenu(popupMenuList);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected class MavenSearchFilesVisitor extends SearchFilesVisitor{

		public boolean visit(IResourceProxy resourceProxy) {
			if ((resourceProxy.getType() == IResource.FILE) && resourceProxy.getName().equals(searchFileName)) {
				IResource resource = resourceProxy.requestResource();

				if (resource.exists() && !resource.getFullPath().toOSString().contains("/target/")) {
					resources.add((IFile)resource);
				}
			}

			return true;
		}
	}	
	
	@SuppressWarnings("unchecked")
	private class LifeayDynamicPopupMenu{
		private Menu _menu;
		private IProject _project;
		private String _goalPrefix;
		
		private Set<String> _goals = ListOrderedSet.decorate(new ArrayList<String>());
		
		public LifeayDynamicPopupMenu(IProject project, String goalPrefix, Menu menu) {
			_project = project;
			_goalPrefix = goalPrefix;
			_menu = menu;
		}
		
		public void addGoal(String goal) {
			_goals.add(goal);
		}

		public IProject getProject() {
			return _project;
		}
		
		public Menu getParentMenu() {
			return _menu;
		}
		
		public String getGoalPrefix() {
			return _goalPrefix;
		}
		
		public Set<String> getGoals(){
			return _goals;
		}
	}
    private String getPluginDescriptorLocation()
    {
        return "META-INF/maven/plugin.xml";
    }
    
    private PluginDescriptor extractPluginDescriptor( Artifact pluginArtifact, Plugin plugin )
    		        throws PluginDescriptorParsingException, InvalidPluginDescriptorException
    {
        PluginDescriptor pluginDescriptor = null;

        File pluginFile = pluginArtifact.getFile();

        try
        {
            if ( pluginFile.isFile() )
            {
                JarFile pluginJar = new JarFile( pluginFile, false );
                try
                {
                    ZipEntry pluginDescriptorEntry = pluginJar.getEntry( getPluginDescriptorLocation() );

                    if ( pluginDescriptorEntry != null )
                    {
                        InputStream is = pluginJar.getInputStream( pluginDescriptorEntry );

                        pluginDescriptor = parsePluginDescriptor( is, plugin, pluginFile.getAbsolutePath() );
                    }
                }
                finally
                {
                    pluginJar.close();
                }
            }
            else
            {
                File pluginXml = new File( pluginFile, getPluginDescriptorLocation() );

                if ( pluginXml.isFile() )
                {
                    InputStream is = new BufferedInputStream( new FileInputStream( pluginXml ) );
                    try
                    {
                        pluginDescriptor = parsePluginDescriptor( is, plugin, pluginXml.getAbsolutePath() );
                    }
                    finally
                    {
                        IOUtil.close( is );
                    }
                }
            }

            if ( pluginDescriptor == null )
            {
                throw new IOException( "No plugin descriptor found at " + getPluginDescriptorLocation() );
            }
        }
        catch ( IOException e )
        {
            throw new PluginDescriptorParsingException( plugin, pluginFile.getAbsolutePath(), e );
        }
        
        pluginDescriptor.setPluginArtifact( pluginArtifact );

        return pluginDescriptor;
    }	
	
	private PluginDescriptor anlayzePlugin(Plugin liferayPlugin, List<RemoteRepository> remotePluginRepositories) {
		RepositorySystem repositorySystem = AetherUtil.newRepositorySystem();
		RepositorySystemSession session = AetherUtil.newRepositorySystemSession(repositorySystem);

		if ( liferayPlugin != null) {
			DefaultArtifact pluginArtifact = toArtifact( liferayPlugin, session );
            try
            {
                ArtifactRequest artifactRequest = new ArtifactRequest( pluginArtifact, remotePluginRepositories, "plugin"  );
                org.eclipse.aether.artifact.Artifact artifact2 = repositorySystem.resolveArtifact( session, artifactRequest ).getArtifact();
                Artifact mavenArtifact = RepositoryUtils.toArtifact( artifact2 );
                return extractPluginDescriptor( mavenArtifact, liferayPlugin );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
		}
		return null;
	}

	private PluginDescriptorBuilder builder = new PluginDescriptorBuilder();
	
	private PluginDescriptor parsePluginDescriptor(
		InputStream is, Plugin plugin, String descriptorLocation)
		throws PluginDescriptorParsingException {

		try {
			Reader reader = ReaderFactory.newXmlReader(is);

			PluginDescriptor pluginDescriptor =
				builder.build(reader, descriptorLocation);

			return pluginDescriptor;
		}
		catch (IOException e) {
			throw new PluginDescriptorParsingException(
				plugin, descriptorLocation, e);
		}
		catch (PlexusConfigurationException e) {
			throw new PluginDescriptorParsingException(
				plugin, descriptorLocation, e);
		}
	}

	private org.eclipse.aether.artifact.DefaultArtifact toArtifact(
		Plugin plugin, RepositorySystemSession session) {

		return new org.eclipse.aether.artifact.DefaultArtifact(
			plugin.getGroupId(), plugin.getArtifactId(), null, "jar",
			plugin.getVersion(),
			session.getArtifactTypeRegistry().get("maven-plugin"));
	}
	
}
