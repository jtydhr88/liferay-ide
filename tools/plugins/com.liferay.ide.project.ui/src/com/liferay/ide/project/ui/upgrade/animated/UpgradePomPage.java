/*******************************************************************************
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
 *
 *******************************************************************************/

package com.liferay.ide.project.ui.upgrade.animated;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.upgrade.ILiferayLegacyProjectUpdater;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.project.ui.upgrade.LiferayUpgradeCompre;
import com.liferay.ide.ui.util.SWTUtil;
import com.liferay.ide.ui.util.UIUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

/**
 * @author Andy Wu
 */
public class UpgradePomPage extends Page
{

    public class UpgradePomElement
    {

        public IProject project;
        public boolean finished;

        public UpgradePomElement( IProject project, boolean isFixed )
        {
            this.project = project;
            this.finished = isFixed;
        }
    }

    private class ProjectLabelProvider extends LabelProvider implements IStyledLabelProvider, IColorProvider
    {

        @Override
        public Image getImage( Object element )
        {
            return ProjectUI.getDefault().getImage( "pom_file.gif" );
        }

        @Override
        public StyledString getStyledText( Object element )
        {
            UpgradePomElement upgadePomelement = (UpgradePomElement) element;
            String projectName = upgadePomelement.project.getName();

            String text = "pom.xml" + " (" + projectName + ")";

            StyledString retVal = new StyledString();

            ColorRegistry colorReg = JFaceResources.getColorRegistry();

            String UPGRADE_POM_FRONT_COLOR = "UPGRADE_POM_FRONT_COLOR";

            Color frontColor = null;

            if( !colorReg.hasValueFor( UPGRADE_POM_FRONT_COLOR ) )
            {
                frontColor = Display.getCurrent().getSystemColor( SWT.COLOR_BLUE );
                colorReg.put( UPGRADE_POM_FRONT_COLOR, frontColor.getRGB() );
            }
            else
            {
                frontColor = colorReg.get( UPGRADE_POM_FRONT_COLOR );
            }

            if( upgadePomelement.finished )
            {
                text += "( Finished )";

                retVal.append( text, StyledString.createColorRegistryStyler( UPGRADE_POM_FRONT_COLOR, null ) );
            }
            else
            {
                retVal.append( text );
            }

            return retVal;
        }

        @Override
        public Color getForeground( Object element )
        {
            if( element instanceof UpgradePomElement )
            {
                UpgradePomElement ele = (UpgradePomElement) element;

                if( ele.finished )
                {
                    return Display.getCurrent().getSystemColor( SWT.COLOR_BLUE );
                }
            }

            return Display.getCurrent().getSystemColor( SWT.COLOR_BLACK );
        }

        @Override
        public Color getBackground( Object element )
        {
            return null;
        }
    }

    private CheckboxTableViewer fTableViewer;

    private Button upgradeButton;

    private ILiferayLegacyProjectUpdater updater;

    private UpgradePomElement[] upgradePomElementsArray = null;

    public UpgradePomPage( Composite parent, int style, LiferayUpgradeDataModel dataModel )
    {
        super( parent, style, dataModel, UPGRADE_POM_PAGE_ID, true );

        GridLayout layout = new GridLayout( 2, false );
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        this.setLayout( layout );

        final GridData descData = new GridData( GridData.FILL_BOTH );
        descData.grabExcessVerticalSpace = true;
        descData.grabExcessHorizontalSpace = true;

        this.setLayoutData( descData );

        fTableViewer = CheckboxTableViewer.newCheckList( this, SWT.BORDER );
        fTableViewer.setContentProvider( new ArrayContentProvider() );
        fTableViewer.setLabelProvider( new DelegatingStyledCellLabelProvider( new ProjectLabelProvider() ) );

        fTableViewer.addDoubleClickListener( new IDoubleClickListener()
        {

            @Override
            public void doubleClick( DoubleClickEvent event )
            {
                handleCompare( (IStructuredSelection) event.getSelection() );
            }
        } );

        final Table table = fTableViewer.getTable();

        final GridData tableData = new GridData( GridData.FILL_BOTH );
        tableData.grabExcessVerticalSpace = true;
        tableData.grabExcessHorizontalSpace = true;
        tableData.horizontalAlignment = SWT.FILL;
        table.setLayoutData( tableData );

        Composite buttonContainer = new Composite( this, SWT.NONE );
        buttonContainer.setLayout( new GridLayout( 1, false ) );
        buttonContainer.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false, 1, 1 ) );

        final Button findButton = new Button( buttonContainer, SWT.NONE );
        findButton.setText( "Find..." );
        findButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false, 1, 1 ) );
        findButton.addListener( SWT.Selection, new Listener()
        {

            @Override
            public void handleEvent( Event event )
            {
                handleFindEvent();
            }
        } );

        final Button selectAllButton = new Button( buttonContainer, SWT.NONE );
        selectAllButton.setText( "Select All" );
        selectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false, 1, 1 ) );
        selectAllButton.addSelectionListener( new SelectionAdapter()
        {

            @Override
            public void widgetSelected( SelectionEvent e )
            {
                fTableViewer.setAllChecked( true );
                setUpgradeButtonEnable();
            }
        } );

        final Button disSelectAllButton = new Button( buttonContainer, SWT.NONE );
        disSelectAllButton.setText( "Deselect All" );
        disSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false, 1, 1 ) );
        disSelectAllButton.addSelectionListener( new SelectionAdapter()
        {

            @Override
            public void widgetSelected( SelectionEvent e )
            {
                fTableViewer.setAllChecked( false );
                setUpgradeButtonEnable();
            }
        } );

        upgradeButton = new Button( buttonContainer, SWT.NONE );
        upgradeButton.setText( "Upgrade Selected" );
        upgradeButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false ) );
        upgradeButton.addListener( SWT.Selection, new Listener()
        {

            @Override
            public void handleEvent( Event event )
            {
                handleUpgradeEvent();
            }
        } );

        fTableViewer.addCheckStateListener( new ICheckStateListener()
        {

            @Override
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                setUpgradeButtonEnable();
            }
        } );
    }

    public void createSpecialDescriptor( Composite parent, int style )
    {
        final String descriptor =
            "This step will guide you to upgrade maven pom.xml files for the following aspects.\n" +
                "  1. Convert and add dependencies for 7.x.\n" +
                "  2. Remove the legacy 6.2 plugins.\n" +
                "  3. Add 7.x maven plugins accroding to project type:\n" +
                "  com.liferay.css.builder -> portlet, com.liferay.portal.tools.theme.builder -> theme, com.liferay.portal.tools.service.builder -> service-builder\n" +
                "Double clicking a file will bring up the compare editor of the original file and the upgraded file.";

        String url = "";

        Link link = SWTUtil.createHyperLink( this, style, descriptor, 1, url );
        link.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false, 2, 1 ) );
    }

    @Override
    public String getPageTitle()
    {
        return "Upgrade POM Files";
    }

    private List<UpgradePomElement> getSelectedElements()
    {
        final Object[] selectedElements = fTableViewer.getCheckedElements();

        List<UpgradePomElement> upgradePomElements = new ArrayList<>();

        if( selectedElements != null )
        {
            for( Object element : selectedElements )
            {
                if( element instanceof UpgradePomElement )
                {
                    UpgradePomElement ele = (UpgradePomElement) element;
                    upgradePomElements.add( ele );
                }
            }
        }

        return upgradePomElements;
    }

    private ILiferayLegacyProjectUpdater getUpdater()
    {
        if( updater == null )
        {
            updater = ProjectCore.getDefault().getLiferayLegacyProjectUpdater();
        }

        return updater;
    }

    private void handleCompare( IStructuredSelection selection )
    {
        UpgradePomElement element = (UpgradePomElement) selection.getFirstElement();

        IProject project = element.project;

        if( project.exists() )
        {
            IPath tmpDirPath = ProjectUI.getDefault().getStateLocation().append( "tmp" );

            File tmpDir = tmpDirPath.toFile();
            tmpDir.mkdirs();

            File tempPomFile = new File( tmpDir, "pom.xml" );

            getUpdater().upgradePomFile( project, tempPomFile );

            IFile pomfile = project.getFile( "pom.xml" );

            final LiferayUpgradeCompre lifeayDescriptorUpgradeCompre =
                new LiferayUpgradeCompre( pomfile.getLocation(), tmpDirPath.append( "pom.xml" ), "pom.xml" );

            lifeayDescriptorUpgradeCompre.openCompareEditor();
        }
        else
        {
            MessageDialog.openInformation( getShell(), "Confirm", "project " + project.getName() + " doesn't exist" );
        }
    }

    private void handleFindEvent()
    {
        IProject[] projectArrys = CoreUtil.getAllProjects();

        List<UpgradePomElement> upgradePomElements = new ArrayList<UpgradePomElement>();

        for( IProject project : projectArrys )
        {
            if( ProjectUtil.isMavenProject( project ) && getUpdater().isNeedUpgrade( project ) )
            {
                upgradePomElements.add( new UpgradePomElement( project, false ) );
            }
        }

        UIUtil.async( new Runnable()
        {

            @Override
            public void run()
            {
                String message = "ok";

                upgradePomElementsArray = upgradePomElements.toArray( new UpgradePomElement[] {} );

                fTableViewer.setInput( upgradePomElementsArray );

                if( upgradePomElementsArray.length < 1 )
                {
                    message = "No pom file needs to be upgraded";
                }

                PageValidateEvent pe = new PageValidateEvent();
                pe.setMessage( message );
                pe.setType( PageValidateEvent.WARNING );

                triggerValidationEvent( pe );
            }
        } );
    }

    private void handleUpgradeEvent()
    {
        try
        {
            List<UpgradePomElement> upgradePomElements = getSelectedElements();

            for( UpgradePomElement element : upgradePomElements )
            {
                getUpdater().upgradePomFile( element.project, null );
                element.finished = true;
            }

            fTableViewer.setInput( this.upgradePomElementsArray );
            fTableViewer.setAllChecked( false );
        }
        catch( Exception e )
        {
            ProjectUI.logError( e );
        }
    }

    private void setUpgradeButtonEnable()
    {
        List<UpgradePomElement> upgradePomElements = getSelectedElements();

        boolean isEnable = true;

        for( UpgradePomElement element : upgradePomElements )
        {
            if( element.finished )
            {
                isEnable = false;
            }
        }

        upgradeButton.setEnabled( isEnable );
    }

}
