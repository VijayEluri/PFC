package org.pfc.web.device;

import java.util.List;

import org.pfc.business.device.Device;
import org.pfc.business.deviceservice.IDeviceService;
import org.pfc.business.util.exceptions.InstanceNotFoundException;
import org.pfc.snmp.SnmpService;
import org.zkoss.gmaps.Gmaps;
import org.zkoss.gmaps.Gmarker;
import org.zkoss.gmaps.event.MapDropEvent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * 
 * @author Sergio García Ramos <sergio.garcia@udc.es>
 *
 */
public class DeviceCRUDController extends GenericForwardComposer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3190271104080945929L;
	
	private Window win;
	private Listbox deviceList;
	private Grid addForm;
	private Gmaps map;
	private Textbox name;
	private Textbox description;
	private Textbox ipAddress;
	private Textbox pubCommunity;
	private Textbox snmpPort;
	private Doublebox lat;
	private Doublebox lng;
	private Label snmpGet;
	private Textbox oid;
	
	private Device current = new Device();
	private Device newDev;
	private IDeviceService deviceService; 
	
	public void setDeviceService(IDeviceService deviceService) {
		this.deviceService = deviceService;
	}	

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		renderMap(map);
	}

	public Device getCurrent() {
		return current;
	}

	public void setCurrent(Device current) {
		this.current = current;
	}
	
	public Device getNewDev() {
		return newDev;
	}

	public void setNewDev(Device newDev) {
		this.newDev = newDev;
	}

	public List<Device> getDevices() {
		return deviceService.findAllDevice();
	}
	
	public String snmpQuery() {
		
		SnmpService snmp = new SnmpService();
		return snmp.snmpGet(current.getPublicCommunity(), current.getIpAddress(), current.getSnmpPort(), oid.getValue());
	}

	private void renderMap(Gmaps m) {
		m.setDoubleClickZoom(true);
		m.setScrollWheelZoom(true);
		m.getChildren().clear();
		List<Device> devices = deviceService.findAllDevice();
		for (Device d:devices){
			Gmarker marker = new Gmarker(d.getName(),d.getLat(),d.getLng());
			marker.setDraggingEnabled(false);
			m.appendChild(marker);
		}
	}
	
	public void onMapDrop$map(MapDropEvent event) {
		lat.setValue(event.getLat());
		lng.setValue(event.getLng());
	}
	
	public void onClick$addTestData() {
		int n = 5;
		for (int i=1; i<=n; i++) {
			Device d = new Device("AP"+i,"AP de prueba "+i,"127.0.0.1","public","161",
					null,43.354891546397745 + Math.random() % 0.02,-8.416385650634766 + Math.random()%0.02);
			deviceService.createDevice(d);
		}
		renderMap(map);
	}	
	
	public void onClick$addDevice() {
		newDev = new Device();
		lat.setValue(map.getLat());
		lng.setValue(map.getLng());
		Gmarker m = new Gmarker("newDevice", lat.getValue(), lng.getValue());
		
		map.appendChild(m);

		win.getFellow("deviceListbox").setVisible(false);
		win.getFellow("addForm").setVisible(true);
	}
	
	public void onClick$editDevice() {

		if (current.getId() != null) {
			newDev = current;
			name.setValue(current.getName());
			description.setValue(current.getDescription());
			ipAddress.setValue(current.getIpAddress());
			pubCommunity.setValue(current.getPublicCommunity());
			snmpPort.setValue(current.getSnmpPort());
			lat.setValue(current.getLat());
			lng.setValue(current.getLng());
			

			map.getChildren().clear();
			Gmarker m = new Gmarker(current.getName(),current.getLat(),current.getLng());
			m.setDraggingEnabled(true);
			map.appendChild(m);
			
			win.getFellow("deviceListbox").setVisible(false);
			win.getFellow("addForm").setVisible(true);	
		}
		else {
			alert("Selecciona el dispositivo que quieres editar");	
		}
	}
	
	public void onClick$save() {

		newDev.setName(name.getValue());
		newDev.setDescription(description.getValue());
		newDev.setIpAddress(ipAddress.getValue());
		newDev.setPublicCommunity(pubCommunity.getValue());
		newDev.setSnmpPort(snmpPort.getValue());
		newDev.setLat(lat.getValue());
		newDev.setLng(lng.getValue());
		
		deviceService.createDevice(newDev);
        
        name.setValue(null);
        description.setValue(null);
        ipAddress.setValue(null);
        pubCommunity.setValue("public");
        snmpPort.setValue("161");
        renderMap(map);
        
        win.getFellow("deviceListbox").setVisible(true);
        win.getFellow("addForm").setVisible(false);
	
	}

	public void onClick$cancel() {
		
		name.setValue(null);
        description.setValue(null);
        ipAddress.setValue(null);
        pubCommunity.setValue("public");
        snmpPort.setValue("161");
        
        win.getFellow("deviceListbox").setVisible(true);
        win.getFellow("addForm").setVisible(false);
        renderMap(map);
	
	}
	
	public void onClick$delDevice() {
		
		try {
			
			if (current.getId() != null) {
				deviceService.removeDevice(current.getId());
				current.setId(null);
				renderMap(map);
			}
			else {
				alert("Selecciona el dispositivo que quieres eliminar");
			}
		} catch (InstanceNotFoundException e) {
			alert("No se ha encontrado el dispositivo seleccionado en la BD");
		}
	}
	
	public void onClick$query() {
		
		if (current.getId() != null) {
			snmpGet.setValue("SNMP request to: "+current.getName()+" - Response: "+ this.snmpQuery());
		}
		else {
			alert("Selecciona el dispositivo que quieres consultar");
		}
	}
}