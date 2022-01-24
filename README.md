# MinecraftServerPinger
A Java class for getting info from a Minecraft server

MinecraftServerPinger is a simple Java class for getting information from a minecraft server.
Currently it can get the server's MOTD, player info, version and favicon.

# Examples

Checking if the server is online:
```java
try {
    new MinecraftServerPinger("SERVER ADDRESS", PORT).connect(TIMEOUT);
    System.out.println("Server is online!");
} catch (Exception e) {
    System.out.println("Cannot connect to the server");
}
```

Getting the server's MOTD:
```java
try {
    MinecraftServerPinger pinger = new MinecraftServerPinger("SERVER ADDRESS", PORT);
    pinger.connect(TIMEOUT);
    System.out.println("MOTD: " + pinger.getDescription());
} catch (Exception e) {
    System.out.println("Cannot connect to the server");
}
```
