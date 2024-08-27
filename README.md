# Luna-Lauf RMS

The *Luna-Lauf RMS* (Luna-Lauf Round Management System) is a distributed system to manage and display the rounds of runners and teams during the Luna-Lauf event.
For more information about the event, take a look at our [website](https://www.lunalauf.de/) (available in german only).

## Hardware Setup

```
  ┌───────────┐                                          
  │ Projector │                                          
  └─────▲─────┘                                          
        │ displays all team & runner scores              
   ┌────┴───┐                                            
   │ CENTER │ manages all data & processes COUNTER inputs
   └────▲───┘                                            
        │                                                
   ┌────▼─────┐                                          
  ┌┴─────────┐│ counts round/shows score                 
  │ COUNTERS ├┘ of scanning runner                       
  └─────▲────┘                                           
        │ transmits runner ID                            
┌───────┴──────┐                                         
│ RFID Scanner │                                         
└──────────────┘                                         
```

### Counter application

Runners can count their rounds or access their individual score using a personalized RFID chip that is scanned at an RFID scanner.
Each scanner is connected to a device running the *Counter app*.
It is in charge of displaying the results of the scan, e.g. by showing a green sign if a round has been successfully counted.
All Counters are connected to a central instance via network, called *Center app*. 

### Center application

The Center is in charge of managing the requests of all connected Counters, as well as the data of all teams and runners.
This also includes points achieved at minigames during the run and sponsoring amounts per team and runner.
The Center can output a scoreboard, which can be displayed on a projector.

In addition to counting rounds via RFID, there is the option to host a Telegram bot.
Runners can register themselves via this bot and use a bot command to communicate a completed round.
