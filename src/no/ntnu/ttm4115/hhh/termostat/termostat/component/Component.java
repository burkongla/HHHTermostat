package no.ntnu.ttm4115.hhh.termostat.termostat.component;

import com.bitreactive.library.mqtt.MQTTConfigParam;
import com.bitreactive.library.mqtt.MQTTMessage;
import com.bitreactive.library.mqtt.robustmqtt.RobustMQTT.Parameters;

import no.ntnu.item.arctis.runtime.Block;

public class Component extends Block {

	public float CurrentTemperature;
	public float DesiredTemperature;
	public java.lang.String newTopic;
	public boolean Status;
	public java.lang.String looPayload;
	public boolean Heating;
	public boolean DoubleTrouble;

	public Parameters MQTTSetup() {
		setLooPayload("");
		this.CurrentTemperature = 20;
		MQTTConfigParam m = new MQTTConfigParam("dev.bitreactive.com");
		m.addSubscribeTopic("hhh/thermostats");
		Parameters p = new Parameters(m);
		return p;
	}

	public void msgHandler(MQTTMessage m) {
		setDoubleTrouble(false);
		String[] message = new String(m.getPayload()).split("\\s+");
		if (message[0].equals("Heating:") && isStatus() && this.CurrentTemperature<=this.DesiredTemperature) {
			this.CurrentTemperature += 1;
			if (this.CurrentTemperature == this.DesiredTemperature) {
				setStatus(false);
			}
			setLooPayload("Current: " + String.valueOf(this.CurrentTemperature));
			setNewTopic("hhh/server");
			setHeating(true);
			setDoubleTrouble(true);
			
		} else if (message[0].equals("Cooling:") && this.CurrentTemperature>this.DesiredTemperature) {
			this.CurrentTemperature -= 0.5;
			System.out.println(CurrentTemperature);
			setLooPayload("Current: " + String.valueOf(this.CurrentTemperature));
			setNewTopic("hhh/server");
			setHeating(false);
			setDoubleTrouble(true);
			
		} else if (message[0].equals("Desired:")) {
			setStatus(true);
			this.DesiredTemperature = Float.valueOf(message[1]);
			
			if (this.CurrentTemperature>=this.DesiredTemperature) {
				setLooPayload("Cooling: -0.5");
				setNewTopic("hhh/heaters");
				
			} else if (this.CurrentTemperature<this.DesiredTemperature) {
				setLooPayload("Heating: 1");
				setNewTopic("hhh/heaters");
			}
		}
		System.out.println("End of msgHandler");
		System.out.println(message[0] + message[1]);
		System.out.println(this.DesiredTemperature);
		System.out.println(this.CurrentTemperature);
		System.out.println(getNewTopic());

	}

	public MQTTMessage createMessage() {
		System.out.println("Creating message.");
		String topic = getNewTopic();
		String payload = getLooPayload();
		byte[] bytes = payload.getBytes();
		MQTTMessage message = new MQTTMessage(bytes, topic);
		message.setQoS(2);
		if(topic.equals("hhh/server")&&isDoubleTrouble()) {
			setNewTopic("hhh/heaters");
			if (isHeating()) {
				setLooPayload("Heating: 1");
			} else if (!isHeating()) {
				setLooPayload("Cooling: -0.5");
			}
		}
		return message;
	}
	
	public float getCurrentTemperature() {
		return CurrentTemperature;
	}

	public void setCurrentTemperature(float currentTemperature) {
		CurrentTemperature = currentTemperature;
	}

	public float getDesiredTemperature() {
		return DesiredTemperature;
	}

	public void setDesiredTemperature(float desiredTemperature) {
		DesiredTemperature = desiredTemperature;
	}

	public void setNewTopic(java.lang.String topic) {
		newTopic = topic;
	}
	
	public java.lang.String getNewTopic() {
		return newTopic;
	}

	public void setStatus(boolean status) {
		Status = status;
	}

	public boolean isStatus() {
		return Status;
	}

	public java.lang.String getLooPayload() {
		return looPayload;
	}

	public void setLooPayload(java.lang.String looPayload) {
		this.looPayload = looPayload;
	}

	public boolean isHeating() {
		return Heating;
	}

	public void setHeating(boolean heating) {
		Heating = heating;
	}

	public boolean isDoubleTrouble() {
		return DoubleTrouble;
	}

	public void setDoubleTrouble(boolean doubleTrouble) {
		DoubleTrouble = doubleTrouble;
	}

}
