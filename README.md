# Mirror
[![Circle CI](https://circleci.com/gh/Nunnery/mirror.svg?style=svg)](https://circleci.com/gh/Nunnery/mirror)

Mirror is a [reflection] library for Bukkit.

## Acquisition
You can acquire Mirror from my Maven repository.

### Maven
```
<repositories>
    <repository>
        <id>nunnery-repo</id>
        <url>http://repo-topplethenun.rhcloud.com/nexus/content/groups/public/</url>
    </repository>
</repositories>

<dependencies>
    <groupId>me.topplethenun</groupId>
    <artifactId>mirror</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependencies>
```

### Gradle
```
repositories {
    maven {
        url "http://repo-topplethenun.rhcloud.com/nexus/content/groups/public/"
    }
}

dependencies {
    compile "me.topplethenun:mirror:0.0.1-SNAPSHOT"
}
```

## Examples
In order to get the `playerConnection` field for a Player, you would do something along the lines of:
```
void send(Player player, String message) {
    Object handle = Mirror.getMethod(player.getClass(), "getHandle").invoke(player);
    Object connection = Mirror.getField(handle.getClass(), "playerConnection").get(handle);
}
```

[reflection]: http://docs.oracle.com/javase/tutorial/reflect/