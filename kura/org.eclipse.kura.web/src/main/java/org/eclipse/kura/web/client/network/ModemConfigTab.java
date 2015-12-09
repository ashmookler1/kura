/**
 * Copyright (c) 2011, 2014 Eurotech and/or its affiliates
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Eurotech
 */
package org.eclipse.kura.web.client.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.web.client.messages.Messages;
import org.eclipse.kura.web.client.resources.Resources;
import org.eclipse.kura.web.client.util.Constants;
import org.eclipse.kura.web.client.util.FailureHandler;
import org.eclipse.kura.web.client.util.FormUtils;
import org.eclipse.kura.web.client.util.MessageUtils;
import org.eclipse.kura.web.shared.model.GwtModemAuthType;
import org.eclipse.kura.web.shared.model.GwtModemInterfaceConfig;
import org.eclipse.kura.web.shared.model.GwtModemSimCardEntry;
import org.eclipse.kura.web.shared.model.GwtNetInterfaceConfig;
import org.eclipse.kura.web.shared.model.GwtSession;
import org.eclipse.kura.web.shared.model.GwtSimCardSlot;
import org.eclipse.kura.web.shared.model.GwtXSRFToken;
import org.eclipse.kura.web.shared.service.GwtNetworkService;
import org.eclipse.kura.web.shared.service.GwtNetworkServiceAsync;
import org.eclipse.kura.web.shared.service.GwtSecurityTokenService;
import org.eclipse.kura.web.shared.service.GwtSecurityTokenServiceAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ModemConfigTab extends LayoutContainer 
{
	private static final Messages MSGS = GWT.create(Messages.class);
	
	private final GwtSecurityTokenServiceAsync gwtXSRFService = GWT.create(GwtSecurityTokenService.class);
	private final GwtNetworkServiceAsync gwtNetworkService = GWT.create(GwtNetworkService.class);
	
	private final ToolTipBox toolTipField = new ToolTipBox("275px", "66px");
	private final String defaultToolTip = "Mouse over enabled items on the left to see help text.";
	private final Map<String, String> defaultDialString = new HashMap<String, String>();
	
	@SuppressWarnings("unused")
    private GwtSession               m_currentSession;

    private boolean                  m_dirty;
    private boolean                  m_initialized;
	private GwtModemInterfaceConfig  m_selectNetIfConfig;
	
	private FormPanel                m_formPanel;
	private LabelField               m_modemModel;
	private LabelField				 m_networkTechnologies;
	private LabelField               m_connectionType;
    private TextField<String>        m_modemIdField;
	private NumberField              m_ifaceNumField;
	
	private Radio					 m_simSlotAradio;
	private Radio					 m_simSlotBradio;
	private RadioGroup				 m_selectSimSlotRadioGroup;
	
	private Button 					 m_refreshSimInfoButton;
	
	private LabelField               m_imsiLabelField;
	private LabelField               m_iccidLabelField;
	private LabelField				 m_subscriberNumberField;
	
	private TextField<String>        m_dialStringField;
	private TextField<String>        m_apnField;
	private SimpleComboBox<String>   m_authTypeCombo;
	private TextField<String>        m_usernameField;
	private TextField<String>        m_passwordField;
	
	private NumberField 			 m_resetTimeoutField;
	
	private Radio 				   	 m_persistRadioTrue;
	private Radio 				   	 m_persistRadioFalse;
	private RadioGroup 			   	 m_persistRadioGroup;
	
	private NumberField 			 m_maxFailField;
	
	private NumberField 			 m_idleField;
	private TextField<String>        m_activeFilterField;
	
	private NumberField 			 m_lcpEchoIntervalField;
	private NumberField				 m_lcpEchoFailureField;
	
	private Radio 				   	 m_enableGpsRadioTrue;
	private Radio 				   	 m_enableGpsRadioFalse;
	private RadioGroup 			   	 m_enableGpsRadioGroup;
    private ComponentPlugin          m_dirtyPlugin;
     
    private class MouseOverListener implements Listener<BaseEvent> {

    	private String  html;
    	
    	public MouseOverListener(String html) {
    		this.html = html;
    	}
		public void handleEvent(BaseEvent be) {
			toolTipField.setText(html);
		}
    	
    }
	
    public ModemConfigTab(GwtSession currentSession, GwtNetInterfaceConfig netIfConfig)
    {
        m_currentSession    = currentSession;
    	m_dirty             = true;
    	m_initialized       = false;
    
    	defaultDialString.put("HE910", "atd*99***1#");
    	defaultDialString.put("DE910", "atd#777");
	    final ModemConfigTab theTab = this;
	    setNetInterface(netIfConfig);
    	m_dirtyPlugin = new ComponentPlugin() {  
    		public void init(Component component) {  
    			component.addListener(Events.Change, new Listener<ComponentEvent>() {  
    				public void handleEvent(ComponentEvent be) {  
    					FormUtils.addDirtyFieldIcon(be.getComponent());    					
    					theTab.fireEvent(Events.Change);
    				}  
    			});  
  	      	}  
  	    };  	    
    }
    
    public void setNetInterface(GwtNetInterfaceConfig netIfConfig)
    {
    	m_dirty = true;
    	if(netIfConfig instanceof GwtModemInterfaceConfig) {
	    	m_selectNetIfConfig = (GwtModemInterfaceConfig) netIfConfig;
    	}
    }
    
    
    public void getUpdatedNetInterface(GwtNetInterfaceConfig updatedNetIf)
    {
        GwtModemInterfaceConfig updatedModemNetIf = (GwtModemInterfaceConfig) updatedNetIf;
        
        // note - status is set in tcp/ip tab
        
    	if (m_formPanel != null) {
    		updatedModemNetIf.setPppNum(m_ifaceNumField.getValue().intValue());
    		
            String modemId = (m_modemIdField.getValue() != null) ? m_modemIdField.getValue() : "";
    		updatedModemNetIf.setModemId(modemId);

    		boolean simSlotAstatus = m_simSlotAradio.getValue().booleanValue();
    		if (simSlotAstatus) {
    			updatedModemNetIf.setActiveSimCardSlot(GwtSimCardSlot.A);
    		} else{
    			updatedModemNetIf.setActiveSimCardSlot(GwtSimCardSlot.B);
    		}
    		
    		String dialString = (m_dialStringField.getValue() != null) ? m_dialStringField.getValue() : "";
    		updatedModemNetIf.setDialString(dialString);
    		
    		String apn = (m_apnField.getValue() != null) ? m_apnField.getValue() : "";
    		updatedModemNetIf.setApn(apn);
    		
    		String authValue = m_authTypeCombo.getSimpleValue();
    		for (GwtModemAuthType auth : GwtModemAuthType.values()) {
    			if (MessageUtils.get(auth.name()).equals(authValue)) {
    				updatedModemNetIf.setAuthType(auth);
    			}
    		}     		
    		
    		if(updatedModemNetIf.getAuthType() != GwtModemAuthType.netModemAuthNONE) {
    		    String username = (m_usernameField.getValue() != null) ? m_usernameField.getValue() : "";
        		updatedModemNetIf.setUsername(username);
    		    
    		    String password = (m_passwordField.getValue() != null) ? m_passwordField.getValue() : "";
        		updatedModemNetIf.setPassword(password);
    		}    
    		
    		updatedModemNetIf.setResetTimeout(m_resetTimeoutField.getValue().intValue());
    		
    		updatedModemNetIf.setPersist(m_persistRadioTrue.getValue().booleanValue());
    		updatedModemNetIf.setMaxFail(m_maxFailField.getValue().intValue());
    		updatedModemNetIf.setIdle(m_idleField.getValue().intValue());
    		
    		String activeFilter = (m_activeFilterField.getValue() != null) ? m_activeFilterField.getValue() : "";
    		updatedModemNetIf.setActiveFilter(activeFilter);
    		
    		updatedModemNetIf.setLcpEchoInterval(m_lcpEchoIntervalField.getValue().intValue());
    		updatedModemNetIf.setLcpEchoFailure(m_lcpEchoFailureField.getValue().intValue());
    		updatedModemNetIf.setGpsEnabled(m_enableGpsRadioTrue.getValue().booleanValue());
    	} else {
    	    if(m_selectNetIfConfig != null) {
    	        Log.debug("Modem config tab not yet rendered, using original values");
    	        
        	    updatedModemNetIf.setPppNum(m_selectNetIfConfig.getPppNum());
        	    updatedModemNetIf.setModemId(m_selectNetIfConfig.getModemId());
        	    updatedModemNetIf.setActiveSimCardSlot(m_selectNetIfConfig.getActiveSimCardSlot());
        	    updatedModemNetIf.setDialString(m_selectNetIfConfig.getDialString());
        	    updatedModemNetIf.setApn(m_selectNetIfConfig.getApn());
        	    updatedModemNetIf.setAuthType(m_selectNetIfConfig.getAuthType());
        	    updatedModemNetIf.setUsername(m_selectNetIfConfig.getUsername());
        	    updatedModemNetIf.setPassword(m_selectNetIfConfig.getPassword()); 
        	    updatedModemNetIf.setResetTimeout(m_selectNetIfConfig.getResetTimeout());
        	    updatedModemNetIf.setPersist(m_selectNetIfConfig.isPersist());
        	    updatedModemNetIf.setMaxFail(m_selectNetIfConfig.getMaxFail());
        	    updatedModemNetIf.setIdle(m_selectNetIfConfig.getIdle());
        	    updatedModemNetIf.setActiveFilter(m_selectNetIfConfig.getActiveFilter());
        	    updatedModemNetIf.setLcpEchoInterval(m_selectNetIfConfig.getLcpEchoInterval());
        	    updatedModemNetIf.setLcpEchoFailure(m_selectNetIfConfig.getLcpEchoFailure());
        	    updatedModemNetIf.setGpsEnabled(m_selectNetIfConfig.isGpsEnabled());
    	    }
    	}
    }

    public boolean isValid() {
    	if (m_formPanel != null) {
    		for (Field<?> field : m_formPanel.getFields()) {
    			if (!field.isValid()) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    public boolean isDirty() {
    	if (m_formPanel == null) {
    		return false;
    	}
        List<Field<?>> fields = m_formPanel.getFields();
        for (int i=0; i<fields.size(); i++) {
        	if (fields.get(i) instanceof Field) {
	            Field<?> field = (Field<?>) fields.get(i);
	            if (field.isDirty()) {
	                return true;
	            }
        	}
        }
        return false;
    }

    protected void onRender(Element parent, int index) 
    { 
        Log.debug("ModemConfigTab - onRender()");
    	super.onRender(parent, index);         
        setLayout(new FitLayout());
        setId("network-modem");
        
        FormData formData = new FormData();
        formData.setWidth(250);
        
        m_formPanel = new FormPanel();
        m_formPanel.setFrame(false);
        m_formPanel.setBodyBorder(false);
        m_formPanel.setHeaderVisible(false);
        m_formPanel.setLayout(new FlowLayout());
        m_formPanel.setStyleAttribute("min-width", "775px");
        m_formPanel.setStyleAttribute("padding-left", "30px");


        FieldSet fieldSet = new FieldSet();
        FormLayout layoutAccount = new FormLayout();
        layoutAccount.setLabelWidth(Constants.LABEL_WIDTH_FORM);
        fieldSet.setLayout(layoutAccount);
        fieldSet.setBorders(false);
        
        //
        // Tool Tip Box
        //
        toolTipField.setText(defaultToolTip);
        fieldSet.add(toolTipField);

        //
        // Modem Model
        // 
        m_modemModel = new LabelField();
        m_modemModel.setName("modemModel");
        m_modemModel.setFieldLabel(MSGS.netModemModel());
        m_modemModel.addPlugin(m_dirtyPlugin);
        m_modemModel.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        fieldSet.add(m_modemModel, formData);

        //
        // Network Technology
        // 
        m_networkTechnologies = new LabelField();
        m_networkTechnologies.setName("networkTechnologies");
        m_networkTechnologies.setFieldLabel(MSGS.netModemNetworkTechnology());
        m_networkTechnologies.addPlugin(m_dirtyPlugin);
        fieldSet.add(m_networkTechnologies, formData);
        
        //
        // Service Type
        // 
        m_connectionType = new LabelField();
        m_connectionType.setName("serviceType");
        m_connectionType.setFieldLabel(MSGS.netModemConnectionType());
        m_connectionType.addPlugin(m_dirtyPlugin);
        fieldSet.add(m_connectionType, formData);
        
        //
        // Modem Identifier
        // 
        m_modemIdField = new TextField<String>();
        m_modemIdField.setAllowBlank(true);
        m_modemIdField.setName("modemId");
        m_modemIdField.setFieldLabel(MSGS.netModemIdentifier());
        m_modemIdField.addPlugin(m_dirtyPlugin);
        m_modemIdField.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        m_modemIdField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipModemIndentifier()));
        m_modemIdField.addStyleName("kura-textfield");
        fieldSet.add(m_modemIdField, formData);
        
        //
        // Interface number
        // 
        m_ifaceNumField = new NumberField();
        m_ifaceNumField.setAllowBlank(false);
        m_ifaceNumField.setName("ifaceNum");
        m_ifaceNumField.setFieldLabel(MSGS.netModemInterfaceNum());
        m_ifaceNumField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipModemInterfaceNumber()));
        m_ifaceNumField.addPlugin(m_dirtyPlugin);
        m_ifaceNumField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if (val < 0) {
            		return MSGS.netModemInvalidInterfaceNum();
            	}
                return null;
            }
        });
        fieldSet.add(m_ifaceNumField, formData);
        
        m_refreshSimInfoButton = new Button(MSGS.netModemRefreshSimCardInfo(), 
				AbstractImagePrototype.create(Resources.INSTANCE.refresh()),
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				m_refreshSimInfoButton.setEnabled(false);
				m_selectSimSlotRadioGroup.setEnabled(false);
				m_imsiLabelField.setEnabled(false);
				m_iccidLabelField.setEnabled(false);
				m_subscriberNumberField.setEnabled(false);
				gwtXSRFService.generateSecurityToken(new AsyncCallback<GwtXSRFToken> () {
					@Override
					public void onFailure(Throwable ex) {
						FailureHandler.handle(ex);
					}

					@Override
					public void onSuccess(GwtXSRFToken token) {
							gwtNetworkService.findSimCardInfo(token, m_selectNetIfConfig.getModemServiceId(), true, false, new AsyncCallback<ListLoadResult<GwtModemSimCardEntry>> () {
				    			public void onSuccess(ListLoadResult<GwtModemSimCardEntry> result) {
				    				m_refreshSimInfoButton.setEnabled(true);
				    				m_selectSimSlotRadioGroup.setEnabled(true);
				    				m_imsiLabelField.setEnabled(true);
				    				m_iccidLabelField.setEnabled(true);
				    				m_subscriberNumberField.setEnabled(true);
				    				List<GwtModemSimCardEntry> gwtModemSimCardEntries = result.getData();
				    				m_imsiLabelField.setValue(MSGS.netModemNoSimCardDetected());
		    						m_iccidLabelField.setValue(MSGS.netModemNoSimCardDetected());
		    						m_subscriberNumberField.setValue(MSGS.netModemNoSimCardDetected());
				    				for (GwtModemSimCardEntry gwtModemSimCardEntry : gwtModemSimCardEntries) {
										if (gwtModemSimCardEntry.isActive()) {
											m_imsiLabelField.setValue(gwtModemSimCardEntry.getInternationalMobileSubscriberIdentity());
											m_iccidLabelField.setValue(gwtModemSimCardEntry.getIntegratedCircuitCardIdentification());
											m_subscriberNumberField.setValue(gwtModemSimCardEntry.getSubscriberNumber());
										}
									}
				    			}
				    			public void onFailure(Throwable caught) {
				    				FailureHandler.handle(caught);
				    			}
				    		});
					}});
			}
		});
        m_refreshSimInfoButton.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        m_refreshSimInfoButton.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipRefreshSimInfo()));
        
        m_simSlotAradio = new Radio();  
        m_simSlotAradio.setItemId("SlotA");
        m_simSlotAradio.setBoxLabel(MSGS.netModemSimSlotAlabel());
        m_simSlotAradio.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipSelectSimSlot()));
        
        m_simSlotBradio = new Radio();  
        m_simSlotBradio.setItemId("SlotB");
        m_simSlotBradio.setBoxLabel(MSGS.netModemSimSlotBlabel());
        m_simSlotBradio.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipSelectSimSlot()));
        
        m_selectSimSlotRadioGroup = new RadioGroup();
        m_selectSimSlotRadioGroup.setName("selectSimSlot");
        m_selectSimSlotRadioGroup.setFieldLabel(MSGS.netModemSelectSimSlot()); 
        m_selectSimSlotRadioGroup.add(m_simSlotAradio);  
        m_selectSimSlotRadioGroup.add(m_simSlotBradio);
        m_selectSimSlotRadioGroup.addPlugin(m_dirtyPlugin);
        m_selectSimSlotRadioGroup.addListener(Events.Change,new Listener<FieldEvent>(){
            @Override public void handleEvent(FieldEvent be) {
    			gwtXSRFService.generateSecurityToken(new AsyncCallback<GwtXSRFToken>() {
    	        	@Override
    				public void onFailure(Throwable ex) {
    					FailureHandler.handle(ex);
    				}
    	        	public void onSuccess(GwtXSRFToken token) {
    	        		gwtNetworkService.findSimCardInfo(token, m_selectNetIfConfig.getModemServiceId(), false, false, new AsyncCallback<ListLoadResult<GwtModemSimCardEntry>> () {
    	        			public void onSuccess(ListLoadResult<GwtModemSimCardEntry> result) {
    	        				List<GwtModemSimCardEntry> gwtModemSimCardEntries = result.getData();
    	        				m_imsiLabelField.setValue(MSGS.netModemNoSimCardDetected());
	    						m_iccidLabelField.setValue(MSGS.netModemNoSimCardDetected());
	    						m_subscriberNumberField.setValue(MSGS.netModemNoSimCardDetected());
    	        				for (GwtModemSimCardEntry gwtModemSimCardEntry : gwtModemSimCardEntries) {
	    	        				if (m_simSlotAradio.getValue() && (gwtModemSimCardEntry.getSimSlot() == GwtSimCardSlot.A)) {
	    	    						m_imsiLabelField.setValue(gwtModemSimCardEntry.getInternationalMobileSubscriberIdentity());
	    	    						m_iccidLabelField.setValue(gwtModemSimCardEntry.getIntegratedCircuitCardIdentification());
	    	    						m_subscriberNumberField.setValue(gwtModemSimCardEntry.getSubscriberNumber());
	    	    						break;
	    	        				}
	    	        				if (m_simSlotBradio.getValue() && (gwtModemSimCardEntry.getSimSlot() == GwtSimCardSlot.B)) {
	    	        					m_imsiLabelField.setValue(gwtModemSimCardEntry.getInternationalMobileSubscriberIdentity());
	    								m_iccidLabelField.setValue(gwtModemSimCardEntry.getIntegratedCircuitCardIdentification());
	    								m_subscriberNumberField.setValue(gwtModemSimCardEntry.getSubscriberNumber());
	    								break;
	    	        				}
    	        				}
    	        			}
    	        			public void onFailure(Throwable caught) {
    	        				FailureHandler.handle(caught);
    	        			}
    	        		});
    	        	}
    	        });
            }
        });
        
        m_imsiLabelField = new LabelField();
        m_imsiLabelField.setName("imsi");
        m_imsiLabelField.setFieldLabel(MSGS.netModemInternationalMobileSubscriberIdentity());
        m_imsiLabelField.addPlugin(m_dirtyPlugin);
        
        m_iccidLabelField = new LabelField();
        m_iccidLabelField.setName("iccid");
        m_iccidLabelField.setFieldLabel(MSGS.netModemIntegratedCircuitCardIdentification());
        m_iccidLabelField.addPlugin(m_dirtyPlugin);
        
        m_subscriberNumberField = new LabelField();
        m_subscriberNumberField.setName("subscriberNumber");
        m_subscriberNumberField.setFieldLabel(MSGS.netModemSubscriberNumber());
        m_subscriberNumberField.addPlugin(m_dirtyPlugin);
        
        //
        // Dial String
        // 
        m_dialStringField = new TextField<String>();
        m_dialStringField.setName("dialString");
        m_dialStringField.setFieldLabel(MSGS.netModemDialString());
        String dialString = "";
        String model = "";
        if (m_selectNetIfConfig != null) {
        	model = m_selectNetIfConfig.getModel();
        	if (model != null && model.length() > 0) {
        		if (model.contains("HE910")) {
        			dialString = defaultDialString.get("HE910");
        		}
        		else if (model.contains("DE910")) {
        			dialString = defaultDialString.get("DE910");
        		}
        		else {
        			dialString ="";
        		}
        	}
        }
        if (dialString.equals("")) {
        	m_dialStringField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipDialStringDefault()));
        }
        else {
        	m_dialStringField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipDialString(dialString)));
        }
        m_dialStringField.addStyleName("kura-textfield");
        m_dialStringField.setAllowBlank(false);
        m_dialStringField.addPlugin(m_dirtyPlugin);
        
        //
        // APN
        // 
        m_apnField = new TextField<String>();
        m_apnField.setName("apn");
        m_apnField.setFieldLabel(MSGS.netModemAPN());
        m_apnField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipApn()));
        m_apnField.addStyleName("kura-textfield");
        m_apnField.setAllowBlank(false);
        /* commented out
		if (m_selectNetIfConfig != null) {
			for (String techType : m_selectNetIfConfig.getNetworkTechnology()) {
				if (techType.equals("HSPA") || techType.equals("HSDPA")
						|| techType.equals("UMTS")
						|| techType.equals("GSM_GPRS")) {
					m_apnField.setAllowBlank(false);
					break;
				}
			}
		}
		*/
        m_apnField.addPlugin(m_dirtyPlugin);
        
        //
    	// Auth Type
        //
        m_authTypeCombo = new SimpleComboBox<String>();
        m_authTypeCombo.setName("authTypeCombo");
        m_authTypeCombo.setFieldLabel(MSGS.netModemAuthType());
        m_authTypeCombo.setEditable(false);
        m_authTypeCombo.setTypeAhead(true);  
        m_authTypeCombo.setTriggerAction(TriggerAction.ALL);
        for (GwtModemAuthType auth : GwtModemAuthType.values()) {
        	m_authTypeCombo.add(MessageUtils.get(auth.name()));
        }
        m_authTypeCombo.setSimpleValue(MessageUtils.get(GwtModemAuthType.netModemAuthNONE.name()));
        m_authTypeCombo.addSelectionChangedListener( new SelectionChangedListener<SimpleComboValue<String>>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
				refreshForm();
			}
		});
        m_authTypeCombo.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipAuthentication()));
        m_authTypeCombo.addStyleName("kura-combobox");
        m_authTypeCombo.addPlugin(m_dirtyPlugin);
        //m_authTypeCombo.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        
        //
        // Username
        // 
        m_usernameField = new TextField<String>();
        m_usernameField.setAllowBlank(true);
        m_usernameField.setName("username");
        m_usernameField.setFieldLabel(MSGS.netModemUsername());
        m_usernameField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipUsername()));
        m_usernameField.addStyleName("kura-textfield");
        m_usernameField.addPlugin(m_dirtyPlugin);
        
        //
        // Password
        // 
        m_passwordField = new TextField<String>();
        m_passwordField.setAllowBlank(true);
        m_passwordField.setPassword(true);
        m_passwordField.setName("password");
        m_passwordField.setFieldLabel(MSGS.netModemPassword());
        m_passwordField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipPassword()));
        m_passwordField.addStyleName("kura-textfield");
        m_passwordField.addPlugin(m_dirtyPlugin);
        
        boolean isHspaModem = false;
        if (m_selectNetIfConfig != null) {
			for (String techType : m_selectNetIfConfig.getNetworkTechnology()) {
				if (techType.equals("HSPA") || techType.equals("HSDPA")
						|| techType.equals("UMTS")
						|| techType.equals("GSM_GPRS")) {
					isHspaModem = true;
					break;
				}
			}
		}
        if (isHspaModem) {
        	fieldSet.add(m_refreshSimInfoButton, formData);
        	fieldSet.add(m_selectSimSlotRadioGroup, formData);
        	fieldSet.add(m_imsiLabelField, formData);
        	fieldSet.add(m_iccidLabelField, formData);
        	fieldSet.add(m_subscriberNumberField, formData);
        }
        fieldSet.add(m_dialStringField, formData);
        if (isHspaModem) {
	        fieldSet.add(m_apnField, formData);
	        fieldSet.add(m_authTypeCombo, formData);
	        fieldSet.add(m_usernameField, formData);
	        fieldSet.add(m_passwordField, formData); 
        }
        
        // reset timeout
        m_resetTimeoutField = new NumberField();
        m_resetTimeoutField.setAllowBlank(false);
        m_resetTimeoutField.setName("resetTimeout");
        m_resetTimeoutField.setFieldLabel(MSGS.netModemResetTimeout());
        m_resetTimeoutField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipResetTimeout()));
        m_resetTimeoutField.addPlugin(m_dirtyPlugin);
        m_resetTimeoutField.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        m_resetTimeoutField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if ((val < 0) || (val == 1)) {
            		return MSGS.netModemInvalidResetTimeout();
            	}
                return null;
            }
        });
        fieldSet.add(m_resetTimeoutField, formData);
        
        m_persistRadioTrue = new Radio();  
        m_persistRadioTrue.setBoxLabel(MSGS.trueLabel());
        m_persistRadioTrue.setItemId("true");
        m_persistRadioTrue.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipPersist()));
        
        m_persistRadioFalse = new Radio();  
        m_persistRadioFalse.setBoxLabel(MSGS.falseLabel());  
        m_persistRadioFalse.setItemId("false");
        m_persistRadioFalse.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipPersist()));
        
        m_persistRadioGroup = new RadioGroup();
        m_persistRadioGroup.setName("modemPersist");
        m_persistRadioGroup.setFieldLabel(MSGS.netModemPersist()); 
        m_persistRadioGroup.add(m_persistRadioTrue);  
        m_persistRadioGroup.add(m_persistRadioFalse);
        m_persistRadioGroup.addPlugin(m_dirtyPlugin);  
        m_persistRadioGroup.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        fieldSet.add(m_persistRadioGroup, formData);
        
        // maxfail
        m_maxFailField = new NumberField();
        m_maxFailField.setAllowBlank(false);
        m_maxFailField.setName("modemMaxFail");
        m_maxFailField.setFieldLabel(MSGS.netModemMaxFail());
        m_maxFailField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipMaxFail()));
        m_maxFailField.addPlugin(m_dirtyPlugin);
        m_maxFailField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if (val <= 0) {
            		return MSGS.netModemInvalidMaxFail();
            	}
                return null;
            }
        });
        fieldSet.add(m_maxFailField, formData);
        
        // idle
        m_idleField = new NumberField();
        m_idleField.setAllowBlank(false);
        m_idleField.setName("modemIdle");
        m_idleField.setFieldLabel(MSGS.netModemIdle());
        m_idleField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipIdle()));
        m_idleField.addPlugin(m_dirtyPlugin);
        m_idleField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if (val < 0) {
            		return MSGS.netModemInvalidIdle();
            	}
                return null;
            }
        });
        fieldSet.add(m_idleField, formData);
        
        // active-filter
        m_activeFilterField = new TextField<String>();
        m_activeFilterField.setAllowBlank(true);
        m_activeFilterField.setName("active-filter");
        m_activeFilterField.setFieldLabel(MSGS.netModemActiveFilter());
        m_activeFilterField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipActiveFilter()));
        m_activeFilterField.addStyleName("esf-textfield");
        m_activeFilterField.addPlugin(m_dirtyPlugin);
        fieldSet.add(m_activeFilterField, formData);    
        
        //
        // LCP Echo Interval
        // 
        m_lcpEchoIntervalField = new NumberField();
        m_lcpEchoIntervalField.setAllowBlank(false);
        m_lcpEchoIntervalField.setName("lcpEchoInterval");
        m_lcpEchoIntervalField.setFieldLabel(MSGS.netModemLcpEchoInterval());
        m_lcpEchoIntervalField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipLcpEchoInterval()));
        m_lcpEchoIntervalField.addPlugin(m_dirtyPlugin);
        m_lcpEchoIntervalField.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        m_lcpEchoIntervalField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if (val < 0) {
            		return MSGS.netModemInvalidLcpEchoInterval();
            	}
                return null;
            }
        });
        fieldSet.add(m_lcpEchoIntervalField, formData);
        
        //
        // LCP Echo Interval
        // 
        m_lcpEchoFailureField = new NumberField();
        m_lcpEchoFailureField.setAllowBlank(false);
        m_lcpEchoFailureField.setName("lcpEchoFailure");
        m_lcpEchoFailureField.setFieldLabel(MSGS.netModemLcpEchoFailure());
        m_lcpEchoFailureField.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipLcpEchoFailure()));
        m_lcpEchoFailureField.addPlugin(m_dirtyPlugin);
        m_lcpEchoFailureField.setValidator( new Validator() {
            public String validate(Field<?> field, String value) {
            	int val = Integer.parseInt(value);
            	if (val < 0) {
            		return MSGS.netModemInvalidLcpEchoFailure();
            	}
                return null;
            }
        });
        fieldSet.add(m_lcpEchoFailureField, formData);
        
        m_enableGpsRadioTrue = new Radio();  
        m_enableGpsRadioTrue.setBoxLabel(MSGS.trueLabel());
        m_enableGpsRadioTrue.setItemId("true");
        m_enableGpsRadioTrue.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipEnableGps()));
        
        m_enableGpsRadioFalse = new Radio();  
        m_enableGpsRadioFalse.setBoxLabel(MSGS.falseLabel());  
        m_enableGpsRadioFalse.setItemId("false");
        m_enableGpsRadioFalse.addListener(Events.OnMouseOver, new MouseOverListener(MSGS.netModemToolTipEnableGps()));
        
        m_enableGpsRadioGroup = new RadioGroup();
        m_enableGpsRadioGroup.setName("modemEnableGps");
        m_enableGpsRadioGroup.setFieldLabel(MSGS.netModemEnableGps()); 
        m_enableGpsRadioGroup.add(m_enableGpsRadioTrue);  
        m_enableGpsRadioGroup.add(m_enableGpsRadioFalse);
        m_enableGpsRadioGroup.addPlugin(m_dirtyPlugin);  
        m_enableGpsRadioGroup.setStyleAttribute("margin-top", Constants.LABEL_MARGIN_TOP_SEPARATOR);
        fieldSet.add(m_enableGpsRadioGroup, formData);
         
	    m_formPanel.add(fieldSet);
	    m_formPanel.setScrollMode(Scroll.AUTO);
	    add(m_formPanel);
	    setScrollMode(Scroll.AUTOX);
	    m_initialized = true;
	}
	    
	public void refresh() 
	{
		Log.debug("ModemConfigTab.refresh()");
		if (m_dirty && m_initialized) {			
	        m_dirty = false;
			if (m_selectNetIfConfig == null) {
				Log.debug("ModemConfigTab.refresh() - resetting"); 
				reset();
	        } else {
	        	Log.debug("ModemConfigTab.refresh() - updating");
				update();				
			}		
		}
	}	
	
	private void update()
	{
		Log.debug("ModemConfigTab - update()");
		for (Field<?> field : m_formPanel.getFields()) {
			FormUtils.removeDirtyFieldIcon(field);
		}

		if (m_selectNetIfConfig != null) {
            
		    m_modemModel.setValue(m_selectNetIfConfig.getManufacturer() + " - " + m_selectNetIfConfig.getModel());
		    
		    List<String> networkTechnologies = m_selectNetIfConfig.getNetworkTechnology();
		    if(networkTechnologies != null && networkTechnologies.size() > 0) {
		    	StringBuilder sbNetworkTechnologies = new StringBuilder();
    		    for(int i = 0; i < networkTechnologies.size(); i++) {
    		    	String networkTechnology = networkTechnologies.get(i);
    		    	sbNetworkTechnologies.append(networkTechnology);
    		    	if (i < (networkTechnologies.size()-1)) {
    		    		sbNetworkTechnologies.append('/');
    		    	}
    		    }
    		    m_networkTechnologies.setValue(sbNetworkTechnologies.toString());
		    } else {
		    	m_networkTechnologies.setValue(MSGS.unknown());
		    }
		    
		    m_connectionType.setValue(m_selectNetIfConfig.getConnectionType());
		    
			m_modemIdField.setValue(m_selectNetIfConfig.getModemId());
			m_modemIdField.setOriginalValue(m_modemIdField.getValue());

            m_ifaceNumField.setValue(m_selectNetIfConfig.getPppNum());
            m_ifaceNumField.setOriginalValue(m_ifaceNumField.getValue());
            
            // TODO
            gwtXSRFService.generateSecurityToken(new AsyncCallback<GwtXSRFToken>() {
	        	@Override
				public void onFailure(Throwable ex) {
					FailureHandler.handle(ex);
				}
	        	public void onSuccess(GwtXSRFToken token) {
	        		gwtNetworkService.findSimCardInfo(token, m_selectNetIfConfig.getModemServiceId(), false, true, new AsyncCallback<ListLoadResult<GwtModemSimCardEntry>> () {
	        			public void onSuccess(ListLoadResult<GwtModemSimCardEntry> result) {
	        				List<GwtModemSimCardEntry> gwtModemSimCardEntries = result.getData();
	        				m_imsiLabelField.setValue(MSGS.netModemNoSimCardDetected());
    						m_iccidLabelField.setValue(MSGS.netModemNoSimCardDetected());
	        				for (GwtModemSimCardEntry gwtModemSimCardEntry : gwtModemSimCardEntries) {
	        					GwtSimCardSlot gwtSimSlot = gwtModemSimCardEntry.getSimSlot();
	        					if (gwtModemSimCardEntry.isActive()) {
	        						m_imsiLabelField.setValue(gwtModemSimCardEntry.getInternationalMobileSubscriberIdentity());
	        						m_iccidLabelField.setValue(gwtModemSimCardEntry.getIntegratedCircuitCardIdentification());
	        						if (gwtSimSlot == GwtSimCardSlot.A) {
	        							m_simSlotAradio.setValue(true);
	        							m_simSlotBradio.setValue(false);
	        						} else if (gwtSimSlot == GwtSimCardSlot.B) {
	        							m_simSlotAradio.setValue(false);
	        							m_simSlotBradio.setValue(true);
	        						}
	        					}
	        				}
	        			}
	        			public void onFailure(Throwable caught) {
	        				FailureHandler.handle(caught);
	        			}
	        		});
	        	}
	        });
            
			m_dialStringField.setValue(m_selectNetIfConfig.getDialString());
			m_dialStringField.setOriginalValue(m_dialStringField.getValue());
			
			m_apnField.setValue(m_selectNetIfConfig.getApn());
			m_apnField.setOriginalValue(m_apnField.getValue());
			
			GwtModemAuthType authType = GwtModemAuthType.netModemAuthNONE;
			if(m_selectNetIfConfig.getAuthType() != null) {
			    authType = m_selectNetIfConfig.getAuthType();
			}
			m_authTypeCombo.setSimpleValue(MessageUtils.get(authType.name()));
			m_authTypeCombo.setOriginalValue(m_authTypeCombo.getValue());
			
			m_usernameField.setValue(m_selectNetIfConfig.getUsername());
			m_usernameField.setOriginalValue(m_usernameField.getValue());
			
			m_passwordField.setValue(m_selectNetIfConfig.getPassword());
			m_passwordField.setOriginalValue(m_passwordField.getValue());	
			
			if (m_selectNetIfConfig.isPersist()) {
				m_persistRadioTrue.setValue(true);
				m_persistRadioTrue.setOriginalValue(m_persistRadioTrue.getValue());
				
				m_persistRadioFalse.setValue(false);
				m_persistRadioFalse.setOriginalValue(m_persistRadioFalse.getValue());
				
				m_persistRadioGroup.setOriginalValue(m_persistRadioTrue);
				m_persistRadioGroup.setValue(m_persistRadioGroup.getValue());
			} else {
				m_persistRadioTrue.setValue(false);
				m_persistRadioTrue.setOriginalValue(m_persistRadioTrue.getValue());

				m_persistRadioFalse.setValue(true);
				m_persistRadioFalse.setOriginalValue(m_persistRadioFalse.getValue());

				m_persistRadioGroup.setOriginalValue(m_persistRadioFalse);
				m_persistRadioGroup.setValue(m_persistRadioGroup.getValue());
			}
			
			m_resetTimeoutField.setValue(m_selectNetIfConfig.getResetTimeout());
			m_resetTimeoutField.setOriginalValue(m_resetTimeoutField.getValue());
			
			m_maxFailField.setValue(m_selectNetIfConfig.getMaxFail());
			m_maxFailField.setOriginalValue(m_maxFailField.getValue());
			
			m_idleField.setValue(m_selectNetIfConfig.getIdle());
			m_idleField.setOriginalValue(m_idleField.getValue());
			
			m_activeFilterField.setValue(m_selectNetIfConfig.getActiveFilter());
			m_activeFilterField.setOriginalValue(m_activeFilterField.getValue());
			
			m_lcpEchoIntervalField.setValue(m_selectNetIfConfig.getLcpEchoInterval());
			m_lcpEchoIntervalField.setOriginalValue(m_lcpEchoIntervalField.getValue());
			
			m_lcpEchoFailureField.setValue(m_selectNetIfConfig.getLcpEchoFailure());
			m_lcpEchoFailureField.setOriginalValue(m_lcpEchoFailureField.getValue());
			
			if (m_selectNetIfConfig.isGpsEnabled()) {
				m_enableGpsRadioTrue.setValue(true);
				m_enableGpsRadioTrue.setOriginalValue(m_enableGpsRadioTrue.getValue());
				
				m_enableGpsRadioFalse.setValue(false);
				m_enableGpsRadioFalse.setOriginalValue(m_enableGpsRadioFalse.getValue());
				
				m_enableGpsRadioGroup.setOriginalValue(m_enableGpsRadioTrue);
				m_enableGpsRadioGroup.setValue(m_enableGpsRadioGroup.getValue());
			} else {
				m_enableGpsRadioTrue.setValue(false);
				m_enableGpsRadioTrue.setOriginalValue(m_enableGpsRadioTrue.getValue());

				m_enableGpsRadioFalse.setValue(true);
				m_enableGpsRadioFalse.setOriginalValue(m_enableGpsRadioFalse.getValue());

				m_enableGpsRadioGroup.setOriginalValue(m_enableGpsRadioFalse);
				m_enableGpsRadioGroup.setValue(m_enableGpsRadioGroup.getValue());
			}
		} else {
			Log.debug("selected Network Interface Config is null");
		}
		
		for (Field<?> field : m_formPanel.getFields()) {
			FormUtils.removeDirtyFieldIcon(field);
		}
		
		refreshForm();
	}
	
	
	private void refreshForm() {
		if (m_formPanel != null) {
			for (Field<?> field : m_formPanel.getFields()) {
				field.setEnabled(true);
			}
			
			String authTypeVal = m_authTypeCombo.getSimpleValue();
            if(authTypeVal == null || authTypeVal.equals(MessageUtils.get(GwtModemAuthType.netModemAuthNONE.name()))) {
                m_usernameField.setEnabled(false);
                m_passwordField.setEnabled(false);
            } else {
                m_usernameField.setEnabled(true);
                m_passwordField.setEnabled(true);
            }
            
            if (m_selectNetIfConfig.isGpsSupported()) {
            	 m_enableGpsRadioTrue.setEnabled(true);
                 m_enableGpsRadioFalse.setEnabled(true);
                 m_enableGpsRadioGroup.setEnabled(true);
            } else {
            	 m_enableGpsRadioTrue.setEnabled(false);
                 m_enableGpsRadioFalse.setEnabled(false);
                 m_enableGpsRadioGroup.setEnabled(false);
            }
            /*
            for (String techType : m_selectNetIfConfig.getNetworkTechnology()) {
				if (techType.equals("EVDO") || techType.equals("CDMA")) {
					m_apnField.setEnabled(false);
					m_authTypeCombo.setEnabled(false);
					m_usernameField.setEnabled(false);
	                m_passwordField.setEnabled(false);
	                break;
				}
			}
			*/
		}
	}
	
	
	private void reset()
	{
		Log.debug("ModemConfigTab: reset()");

		m_modemIdField.setValue("");
		m_modemIdField.setOriginalValue(m_modemIdField.getValue());
		
        m_ifaceNumField.setValue(null);
        m_ifaceNumField.setOriginalValue(m_ifaceNumField.getValue());
        
		m_dialStringField.setValue("");
		m_dialStringField.setOriginalValue(m_dialStringField.getValue());
		
		m_apnField.setValue("");
		m_apnField.setOriginalValue(m_apnField.getValue());
		
		m_authTypeCombo.setSimpleValue(MessageUtils.get(GwtModemAuthType.netModemAuthNONE.name()));
		m_authTypeCombo.setOriginalValue(m_authTypeCombo.getValue());
		
		m_usernameField.setValue("");
		m_usernameField.setOriginalValue(m_usernameField.getValue());
		
		m_passwordField.setValue("");
		m_passwordField.setOriginalValue(m_passwordField.getValue());	
		
		m_resetTimeoutField.setValue(null);
		m_resetTimeoutField.setOriginalValue(m_resetTimeoutField.getValue());
		
		m_maxFailField.setValue(null);
		m_maxFailField.setOriginalValue(m_maxFailField.getValue());
		
		m_idleField.setValue(null);
		m_idleField.setOriginalValue(m_idleField.getValue());
		
		m_activeFilterField.setValue("");
		m_activeFilterField.setOriginalValue(m_activeFilterField.getValue());
		
		m_lcpEchoIntervalField.setValue(null);
		m_lcpEchoIntervalField.setOriginalValue(m_lcpEchoIntervalField.getValue());		
		
		m_lcpEchoFailureField.setValue(null);
		m_lcpEchoFailureField.setOriginalValue(m_lcpEchoFailureField.getValue());
		
		m_enableGpsRadioGroup.setValue(m_enableGpsRadioFalse);
		m_enableGpsRadioGroup.setOriginalValue(m_enableGpsRadioGroup.getValue());

		update();
	}
}