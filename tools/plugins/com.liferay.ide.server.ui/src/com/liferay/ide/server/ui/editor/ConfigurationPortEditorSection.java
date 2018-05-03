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

package com.liferay.ide.server.ui.editor;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.server.core.ILiferayServerWorkingCopy;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalBundleConfiguration;
import com.liferay.ide.server.core.portal.PortalRuntime;
import com.liferay.ide.server.core.portal.PortalServerDelegate;
import com.liferay.ide.server.ui.LiferayServerUI;
import com.liferay.ide.server.ui.cmd.ModifyPortCommand;
import com.liferay.ide.server.util.ServerUtil;
import com.liferay.ide.server.util.SocketUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.util.ServerLifecycleAdapter;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * @author Simon Jiang
 */
public class ConfigurationPortEditorSection extends ServerEditorSection {

	public ConfigurationPortEditorSection() {
	}

	public void createSection(Composite parent) {
		super.createSection(parent);

		final FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		final Section section = toolkit.createSection(
			parent,
			ExpandableComposite.TWISTIE |
			ExpandableComposite.EXPANDED |
			ExpandableComposite.TITLE_BAR |
			Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);

		section.setText("Ports");
		section.setDescription("Modify the server ports.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		// ports

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();

		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		ports = toolkit.createTable(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);

		ports.setHeaderVisible(true);
		ports.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();

		TableColumn col = new TableColumn(ports, SWT.NONE);

		col.setText("Port Name");

		ColumnWeightData colData = new ColumnWeightData(15, 150, true);

		tableLayout.addColumnData(colData);

		col = new TableColumn(ports, SWT.NONE);

		col.setText("Port Number");

		colData = new ColumnWeightData(8, 80, true);

		tableLayout.addColumnData(colData);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);

		data.widthHint = 230;
		data.heightHint = 100;
		ports.setLayoutData(data);
		ports.setLayout(tableLayout);

		viewer = new TableViewer(ports);

		viewer.setColumnProperties(new String[] {"name", "port"});

		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.setLabelProvider(
			new ColumnLabelProvider() {

				@Override
				public String getToolTipText(Object element) {
					return _getTableItemValidationTooltip((LiferayServerPort)element);
				}

			});

		Hyperlink setDefault = toolkit.createHyperlink(composite, "Restore Default Setting", SWT.WRAP);

		setDefault.addHyperlinkListener(
			new HyperlinkAdapter() {

				public void linkActivated(HyperlinkEvent e) {
					List<LiferayServerPort> defaultPorts = portalBundleConfiguration.readDefaultPorts(
						portalRuntime.getPortalBundle());

					for (LiferayServerPort port : defaultPorts) {
						execute(
							new ModifyPortCommand(
								portalBundleConfiguration, portalSeverDelgate, port.getId(), port.getPort()));
					}
				}

			});

		initialize();
	}

	public void dispose() {
		if (portalBundleConfiguration != null) {
			portalBundleConfiguration.removePropertyChangeListener(listener);
		}
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		IRuntime runtime = server.getRuntime();

		portalRuntime = (PortalRuntime)runtime.loadAdapter(PortalRuntime.class, new NullProgressMonitor());

		try {
			portalSeverDelgate = (PortalServerDelegate)server.getAdapter(PortalServerDelegate.class);

			portalBundleConfiguration = portalSeverDelgate.initBundleConfiguration();
		}
		catch (Exception e) {
		}

		addChangeListener();
		initialize();
		ServerCore.addServerLifecycleListener(
			new ServerLifecycleAdapter() {

				public void serverRemoved(IServer server) {
					IPath defaultPorts = _getServerDefaultPortsFile();

					if (FileUtil.exists(defaultPorts)) {
						File portFile = defaultPorts.toFile();

						portFile.delete();
					}
				}

			});

		_saveDefaultPorsts();
	}

	protected void addChangeListener() {
		listener = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (PortalBundleConfiguration.MODIFY_PORT_PROPERTY.equals(event.getPropertyName())) {
					final String id = (String)event.getOldValue();
					Integer newValue = (Integer)event.getNewValue();

					final int value = Integer.parseInt(newValue.toString());

					changePortNumber(id, value);
				}
			}

		};

		portalBundleConfiguration.addPropertyChangeListener(listener);
	}

	protected void changePortNumber(String id, int port) {
		final TableItem[] items = ports.getItems();

		for (int i = 0; i < items.length; i++) {
			try {
				LiferayServerPort serverPort = (LiferayServerPort)items[i].getData();

				if (id.equals(serverPort.getId())) {
					final LiferayServerPort changedPort = new LiferayServerPort(
						id, serverPort.getName(), port, serverPort.getProtocol(), serverPort.getStoreLocation());

					if (!_getOriginalPort(changedPort) &&
						(!_validPort(items[i], changedPort) || !SocketUtil.isPortAvailable(String.valueOf(port)) ||
						 _isExistedServerPort(changedPort))) {

						items[i].setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT_WARNING));
					}
					else {
						items[i].setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT));
						IMessageManager messageManager = getManagedForm().getMessageManager();

						messageManager.removeMessage(items[i]);
					}

					items[i].setData(changedPort);
					items[i].setText(1, port + "");

					portalSeverDelgate.applyChange(changedPort, new NullProgressMonitor());
					_refreshPortsData(serverPort.getPort());
					return;
				}
			}
			catch (Exception e) {
				LiferayServerUI.logError(e);
			}
		}
	}

	protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		if (_propertyListeners == null) {
			return;
		}

		final PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

		final Iterator<PropertyChangeListener> iterator = _propertyListeners.iterator();

		while (iterator.hasNext()) {
			try {
				final PropertyChangeListener listener = (PropertyChangeListener)iterator.next();

				listener.propertyChange(event);
			}
			catch (Exception e) {
				LiferayServerUI.logError("Error firing property change event", e);
			}
		}
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		try {
			if (ports == null) {
				return;
			}

			ports.removeAll();
			List<LiferayServerPort> liferayServerPorts = portalSeverDelgate.getLiferayServerPorts();

			Iterator<LiferayServerPort> iterator = liferayServerPorts.iterator();

			while (iterator.hasNext()) {
				final LiferayServerPort port = (LiferayServerPort)iterator.next();

				final TableItem item = new TableItem(ports, SWT.NONE);

				String portStr = "-";

				if (port.getPort() >= 0) {
					portStr = port.getPort() + "";
				}

				String[] s = {port.getName(), portStr};

				item.setText(s);

				if (!_validPort(item, port) || _isExistedServerPort(port)) {
					item.setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT_WARNING));
				}
				else {
					item.setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT));
				}

				item.setData(port);
			}

			if (readOnly) {
				viewer.setCellEditors(new CellEditor[] {null, null});
				viewer.setCellModifier(null);
			}
			else {
				setupPortEditors();
			}
		}
		catch (Exception e) {
			LiferayServerUI.logError(e);
		}
	}

	protected void setupPortEditors() {
		viewer.setCellEditors(new CellEditor[] {null, new TextCellEditor(ports)});

		ICellModifier cellModifier = new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return "port".equals(property);
			}

			public Object getValue(Object element, String property) {
				final LiferayServerPort sp = (LiferayServerPort)element;

				return sp.getPort() < 0 ? "-" : sp.getPort() + "";
			}

			public void modify(Object element, String property, Object value) {
				try {
					final LiferayServerPort sp = (LiferayServerPort)((Item)element).getData();

					final int port = Integer.parseInt((String)value);

					if (sp.getPort() != port) {
						execute(new ModifyPortCommand(portalBundleConfiguration, portalSeverDelgate, sp.getId(), port));
					}
				}
				catch (Exception ex) {
				}
			}

		};

		viewer.setCellModifier(cellModifier);

		if (CoreUtil.isWindows()) {
			ports.addSelectionListener(
				new SelectionAdapter() {

					public void widgetSelected(SelectionEvent event) {
						try {
							int selectionIndex = ports.getSelectionIndex();

							TableItem item = ports.getItem(selectionIndex);

							viewer.editElement(item.getData(), 1);
						}
						catch (Exception e) {
						}
					}

				});
		}
	}

	protected ILiferayServerWorkingCopy liferayServer;
	protected PropertyChangeListener listener;
	protected PortalBundleConfiguration portalBundleConfiguration;
	protected PortalRuntime portalRuntime;
	protected PortalServerDelegate portalSeverDelgate;
	protected Table ports;
	protected boolean updating;
	protected TableViewer viewer;

	private boolean _getOriginalPort(final LiferayServerPort newPort) {
		for (LiferayServerPort port : portalSeverDelgate.getLiferayServerPorts()) {
			String oldPortId = port.getId();

			if (oldPortId.equals(newPort.getId())) {
				if (port.getPort() == newPort.getPort()) {
					return true;
				}

				return false;
			}
		}

		return false;
	}

	private IPath _getServerDefaultPortsFile() {
		String serverId = server.getId();

		String newServerId = serverId.replace(" ", "_");

		String serverDefaultPortsJsonFile = new String(newServerId + "_default_ports.json");

		return _pluginPath.append(serverDefaultPortsJsonFile);
	}

	private String _getTableItemValidationTooltip(final LiferayServerPort serverPort) {
		boolean portAvailable = SocketUtil.isPortAvailable(String.valueOf(serverPort.getPort()));
		boolean usedForOtherPort = _isExistedServerPort(serverPort);

		if (!portAvailable) {
			final StringBuffer sb = new StringBuffer();

			sb.append(serverPort.getPort());
			sb.append(" is being used by other application.");

			return sb.toString();
		}

		if (usedForOtherPort) {
			final StringBuffer sb = new StringBuffer();

			sb.append(serverPort.getPort());
			sb.append(" is used for other protocol.");

			return sb.toString();
		}

		final List<String> serverLists = ServerUtil.checkUsingPorts(server.getName(), serverPort);

		if (ListUtil.isNotEmpty(serverLists)) {
			final StringBuffer sb = new StringBuffer();

			sb.append(serverPort.getPort());
			sb.append(" is being used at: ");

			for (int i = 0; i < serverLists.size(); i++) {
				if (i > 1) {
					sb.append("...");

					break;
				}

				sb.append("<");
				sb.append(serverLists.get(i));
				sb.append(">");
			}

			return sb.toString();
		}

		return null;
	}

	private boolean _isExistedServerPort(final LiferayServerPort newPort) {
		for (LiferayServerPort port : portalSeverDelgate.getLiferayServerPorts()) {
			String portName = port.getName();

			if ((port.getPort() == newPort.getPort()) && !portName.equals(newPort.getName())) {
				return true;
			}
		}

		return false;
	}

	private void _refreshPortsData(int port) {
		final TableItem[] items = ports.getItems();

		for (int i = 0; i < items.length; i++) {
			try {
				LiferayServerPort serverPort = (LiferayServerPort)items[i].getData();

				if (port == serverPort.getPort()) {
					if (!_getOriginalPort(serverPort) &&
						(!_validPort(items[i], serverPort) || !SocketUtil.isPortAvailable(String.valueOf(port)) ||
						 _isExistedServerPort(serverPort))) {

						items[i].setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT_WARNING));
					}
					else {
						items[i].setImage(LiferayServerUI.getImage(LiferayServerUI.IMG_PORT));
						IMessageManager messageManager = getManagedForm().getMessageManager();

						messageManager.removeMessage(items[i]);
					}

					items[i].setData(serverPort);
					items[i].setText(1, port + "");
				}
			}
			catch (Exception e) {
				LiferayServerUI.logError(e);
			}
		}
	}

	private void _saveDefaultPorsts() {
		IPath serverDefaultPortsJsonFile = _getServerDefaultPortsFile();

		if (FileUtil.notExists(serverDefaultPortsJsonFile)) {
			try {
				File defaultPortsFile = new File(serverDefaultPortsJsonFile.toOSString());
				List<LiferayServerPort> liferayServerPorts = portalSeverDelgate.getLiferayServerPorts();

				final ObjectMapper mapper = new ObjectMapper();

				mapper.writeValue(defaultPortsFile, liferayServerPorts);
			}
			catch (IOException ioe) {
				LiferayServerUI.logError("Failed to save server default ports inforamion", ioe);
			}
		}
	}

	private boolean _validPort(TableItem item, LiferayServerPort serverPort) {
		final int port = serverPort.getPort();

		if ((port < 0) || (port > 65535)) {
			IMessageManager messageManager = getManagedForm().getMessageManager();

			messageManager.addMessage(item, "Port must to be a number from 1~65535.", item, IMessageProvider.ERROR);

			return false;
		}

		final List<String> serverLists = ServerUtil.checkUsingPorts(server.getName(), serverPort);
		IMessageManager messageManager = getManagedForm().getMessageManager();

		if (ListUtil.isNotEmpty(serverLists)) {
			StringBuffer sb = new StringBuffer();

			sb.append(serverPort.getPort());
			sb.append(" is being used at: ");

			for (String serverName : serverLists) {
				sb.append("<");
				sb.append(serverName);
				sb.append(">");
			}

			messageManager.addMessage(item, sb.toString(), null, IMessageProvider.WARNING);

			return false;
		}
		else {
			messageManager.removeMessage(item);
		}

		return true;
	}

	private IPath _pluginPath = LiferayServerCore.getDefault().getStateLocation();
	private transient List<PropertyChangeListener> _propertyListeners;

}