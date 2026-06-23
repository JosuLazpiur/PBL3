package kodigoa;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublish implements MqttCallback {
    public static final int QoS = 2; // QoS maila ezartzen da (Kalitatearen Zerbitzua): 2 - "Behin eta bakarrik"
    public static final String TOPIC_ALARMA = "alarma"; // Tópikoa non mezuak jasoko ditugun
    private final MqttClient client; // MqttClient objektua, MQTT komunikazioa kudeatzen duena
    private volatile double valueTemperature; // Tenperatura balioa gordetzeko aldagaiaren deklarazioa

    // MqttPublish klasearen konstruktorea. Brokerra eta clientId bat jasotzen ditu.
    public MqttPublish(String broker, String clientId) throws MqttException {
        this.valueTemperature = 0.0; // Tenperatura balioaren hasierako balioa
        MemoryPersistence persistence = new MemoryPersistence(); // Memoria erabiliko da MQTT-persistentzia gordetzeko
        this.client = new MqttClient(broker, clientId, persistence); // MqttClient objektua sortzen da broker eta clientId-rekin
        MqttConnectOptions connOpts = new MqttConnectOptions(); // Konexio aukerak definitzen ditugu
        connOpts.setCleanSession(true); // Clean session: konektatu eta deskonektatu artean egoera gorde gabe
        System.out.println("[MQTT] Connecting to broker: " + broker); // Broker-era konektatzen hasi gara
        this.client.connect(connOpts); // MQTT broker-era konektatzen gara
        System.out.println("[MQTT] Connected"); // Konekzioa egon daitekeela adierazten da
        System.out.println("[MQTT] Topic " + TOPIC_ALARMA); // Topikoa "alarma" duela adierazten da
        client.subscribe(TOPIC_ALARMA, QoS); // Tópikoa "alarma"-rako MQTT-suskripzioa sortzen da
        System.out.println("[MQTT] Ready"); // MQTT-k erabiltzeko prest dagoela adierazten da
        this.client.setCallback(this); // Callback funtzioa ezartzen da
    }

    // Konektatutako MQTT klienta deskonektatzeko metodoa
    public void disconnect() throws MqttException {
        this.client.disconnect(); // Konekzioa eten egiten da
    }

    // Mezu bat argitaratzeko metodoa
    void publish(String content) throws MqttException {
        MqttMessage message = new MqttMessage(content.getBytes()); // Mezuaren edukia bihurtzen da MqttMessage objektu batean
        message.setQos(QoS); // QoS maila ezartzen da (kalitatearen zerbitzua)
        this.client.publish(TOPIC_ALARMA, message); // Mezu bat argitaratzen da "alarma" tópicoan
    }

    // Tenperatura balioaren irakurketarako metodoa
    public double getTemperature() {
        return this.valueTemperature; // Tenperatura balioa bueltatzen du
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    // MQTT konexioa galdu denean deitzen den metodoa
    @Override
    public void connectionLost(Throwable cause) {
        // Konektatu zauden brokerrarekin konexioa galdu denean deitzen da
        // Hemen aplikazioak berreskurapena egin dezake
        System.err.println("Connection to MQTT broker lost!" + cause); // Konektibitate galera abisua
        System.exit(1); // Programaren amaiera
    }

    // Mezu bat entregatu denean deitzen den metodoa
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Mezuaren entregatzea osatzean deitzen da
        // Token honek zerbitzariari bidalitako mezuari buruzko informazioa du
    }

    // Mezu bat iritsi denean deitzen den metodoa
    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        // MQTT zerbitzaritik mezu bat iritsi denean deitzen da
        String content = new String(message.getPayload()); // Mezuaren edukia string batean bihurtzen da
        switch(topic) { // Topikoaren arabera lan egingo dugu
            case TOPIC_ALARMA: // "alarma" topikoan iritsi den mezuari dagokion kasua
                this.valueTemperature = Double.parseDouble(content); // Mezuaren edukia tenperatura balio gisa interpretatzen da
                System.out.println("Alarma: " + this.valueTemperature); // Tenperatura balioa kontsolan erakusten da
                break;
            default: // Beste topiko batetik datorren mezu bat
                break;
        }
    }
}