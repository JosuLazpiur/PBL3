package kodigoa;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

// MqttSuscribe klasea, MqttCallback inplementatzen du, MQTT mezuekin harpidetza eta mezularitza kudeatzeko.
public class MqttSuscribe implements MqttCallback {

    // MQTT mezularitzarako QoS (Zerbitzuaren Kalitatea)
    public static final int QoS = 2;

    // Kudeatuko diren MQTT gaiak (topics)
    public static final String TOPIC_GAS = "gas";
    public static final String TOPIC_JENDEA = "infragorri";

    // MQTT bezeroa (client)
    public final MqttClient client;
    private volatile double valueTemperature; // Mezutik jasotako azken balioa
    PropertyChangeSupport konektorea; // Entzuleei aldaketak jakinarazteko
    private String currentTopic; // Azken mezua jasotako gaia (topic)

    // Eraikitzailea: MQTT zerbitzarira konektatzen da
    public MqttSuscribe(String broker, String clientId) throws MqttException {
        this.valueTemperature = 0.0; // Hasierako balioa

        // Memorian gordetzea erabiliko da mezu ez-iraunkorrak kudeatzeko
        MemoryPersistence persistence = new MemoryPersistence();
        this.client = new MqttClient(broker, clientId, persistence);

        // Konektatzeko aukerak
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true); // Aurreko saioak ez gordetzea

        // Objektu hau callback-en kudeatzaile bezala ezarri
        this.client.setCallback(this);

        // MQTT zerbitzarira konektatu
        System.out.println("[MQTT] Connecting to broker: " + broker);
        this.client.connect(connOpts);
        System.out.println("[MQTT] Connected");

        // Gaietara harpidetu
        System.out.println("[MQTT] Subscribe " + TOPIC_GAS);
        System.out.println("[MQTT] Subscribe " + TOPIC_JENDEA);
        client.subscribe(TOPIC_GAS, QoS);
        client.subscribe(TOPIC_JENDEA, QoS);
        System.out.println("[MQTT] Ready");

        // Aldaketak jakinarazteko laguntzailea inicializatu
        konektorea = new PropertyChangeSupport(this);
    }

    // Entzulea jasotzeko 
    public MqttClient getClient() {
        return client;
    }

    // Entzule bat gehitzeko metodoa
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        konektorea.addPropertyChangeListener(listener);
    }

    // Entzule bat kentzeko metodoa
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        konektorea.removePropertyChangeListener(listener);
    }

    // MQTT zerbitzaritik deskonektatzeko metodoa
    public void disconnect() throws MqttException {
        this.client.disconnect();
    }

    // Mezu bat argitaratzeko metodoa
    void publish(String topic, String content) throws MqttException {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(QoS);
        this.client.publish(topic, message);
    }

    // Balioa itzultzeko metodoa
    public double getTemperature() {
        return this.valueTemperature;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // Konexioa galdu denean exekutatzen den metodoa
        System.err.println("Connection to MQTT broker lost! " + cause);
        System.exit(1);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Mezu bat helarazi denean exekutatzen den metodoa
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        // Mezu bat jaso denean exekutatzen den metodoa
        this.currentTopic = topic;
        String content = new String(message.getPayload());

        try {
            // Mezuan dagoen lehenengo zenbakia aurkitzeko regex erabiltzen da
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                // Aurkitutako lehen zenbakia lortu
                double value = Double.parseDouble(matcher.group());

                switch (topic) {
                    case TOPIC_GAS:
                        this.valueTemperature = value;
                        System.out.println("Gasa " + client.getServerURI() + ": " + this.valueTemperature);
                        konektorea.firePropertyChange("aldatuGasa", client.getServerURI(), this.valueTemperature);
                        break;
                    case TOPIC_JENDEA:
                        this.valueTemperature = value;
                        System.out.println("Jendea " + client.getServerURI() + ": " + this.valueTemperature);
                        konektorea.firePropertyChange("aldatuJendea", client.getServerURI(), this.valueTemperature);
                        break;
                    default:
                        break;
                }
            } else {
                System.err.println("No valid numbers found in message: " + content);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format for message: " + content);
        }
    }

    // Azken mezua jasotako gaia lortzeko metodoa
    public String getTopic() {
        return currentTopic;
    }

}
