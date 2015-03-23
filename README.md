[![Build Status](https://travis-ci.org/frensley/springboot-mqtt-demo.svg?branch=master)](https://travis-ci.org/frensley/springboot-mqtt-demo)

### [MQTT:](http://owntracks.org/)
> MQTT stands for MQ Telemetry Transport. It is a publish/subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. The design principles are to minimise network bandwidth and device resource requirements whilst also attempting to ensure reliability and some degree of assurance of delivery. These principles also turn out to make the protocol ideal of the emerging “machine-to-machine” (M2M) or “Internet of Things” world of connected devices, and for mobile applications where bandwidth and battery power are at a premium.

### [OwnTracks:](http://mqtt.org/faq)
> OwnTracks allows you to keep track of your own location. You can build your private location diary or share it with your family and friends. OwnTracks is open-source and uses open protocols for communication so you can be sure your data stays secure and private.

This project demonstrates the use of MQTT as a lightweight message protocol to track gps information and visualize it on a Google map.


![screen-shot-1](../master/doc/screen-shot-1.png)

#### Build Instructions
1. ``git clone https://github.com/frensley/springboot-mqtt-demo.git``
1. ``cd springboot-mqtt-demo``
1. ``./gradlew build``
1. ``./gradlew bootRun``
1. Use your browser to open http://localhost:8080

#### Owntracks Client Instructions
1. Download [OwnTracks](http://owntracks.org/) for your mobile device
1. Access settings menu
1. Deactivate TLS
1. Deactivate Auth
1. Enter the IP address or Host name of the machine in the Host field
1. Enter a unique name in the DeviceID field

To publish Owntracks location use Location Monitoring Mode Menu (second icon from left on top of OwnTracks tab).
Location publish can be done using the "Publish Now" or "Move Mode" selections.

#### To-do:
- More unit testing
- Documentation
- Better UI experience
- Better broker/messaging solution ([ActiveMQ](http://activemq.apache.org/mqtt.html))
