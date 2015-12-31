package com.liferay.ide.project.ui.tests;

import com.liferay.ide.ui.tests.UIBase;

/**
 * @author Ying Xu
 */

public interface ServiceBuilderWizard extends UIBase {

	public final String LABEL_PROJECT_NAME = "Package path:";
	public final String LABEL_NAMESPACE = "Namespace:";
	public final String LABEL_AUTHOR = "Author";
	public final String LABEL_INCLUDE_SAMPLE_ENTITY = "Include sample entity in new file.";
	public final String LABEL_PLUGIN_PROJECT = "Plugin project:";
	
	public final String MENU_PORTLET = "Portlet";
	
	public final String TOOLTIP_BROWSE1 = "Browse...";
	public final String TOOLTIP_CREATE_LIFERAY_PROJECT = "Create a new Liferay Plugin Project";
	public final String TOOLTIP_NEW_LIFERAY_SERVICE_BUILDER = "New Liferay Service Builder";
    public final String TEXT_ENTER_PROJECT_NAME_SERVICEBUILDER = " Enter a project name.";
    public final String TEXT_NEW_SERVICE_BUILDER_XML_FILE = "Create a new service builder xml file in a project.";
    public final String TEXT_HAS_SERVICE_BUILDER_XML_FILE_MESSAGE = 
                    " Project already contains service.xml file, please select another project.";
    
    public final int INDEX_VALIDATION_MESSAGE = 4;
}
