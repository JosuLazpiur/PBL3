Vigilab - IoT Gas Monitoring System

🏭 What is it?

Vigilab is an IoT safety monitoring system designed for chemical laboratories. It reads toxic gas levels and detects human presence, transmitting the telemetry in real-time to a central Java Desktop dashboard to prevent accidents and trigger evacuation alarms.

🛠️ Technologies Used

Embedded Systems: C, STM32CubeIDE, Azure RTOS, NetXDuo

Software: Java (Swing UI)

Networking & Comms: MQTT Pub/Sub, Cisco Packet Tracer (VLAN, ACL, EIGRP)

Hardware: STM32F429 Board, MQ-7 Gas Sensor, PIR Motion Sensor

✨ Key Features

Real-Time Telemetry: STM32 microcontrollers publish sensor data to an MQTT broker over an Ethernet connection.

Desktop Dashboard: A Java-based GUI that subscribes to the MQTT broker, displaying live charts, historical data, and a real-time lab map.

Enterprise Network Design: The system relies on a secure network topology designed in Packet Tracer, featuring strict ACLs, Inter-VLAN routing, and VPN (OpenVPN) configurations.

PID Control Simulation: Simulink models developed to test gas extraction fan voltages using Proportional-Integral-Derivative controllers.
