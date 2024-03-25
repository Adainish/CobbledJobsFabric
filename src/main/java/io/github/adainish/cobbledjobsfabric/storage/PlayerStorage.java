package io.github.adainish.cobbledjobsfabric.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobbledjobsfabric.CobbledJobsFabric;
import io.github.adainish.cobbledjobsfabric.obj.data.Player;
import io.github.adainish.cobbledjobsfabric.util.Adapters;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStorage
{

    public Database database;

    public void saveAll()
    {
        CobbledJobsFabric.getLog().warn("Saving player data...");
        CobbledJobsFabric.dataWrapper.playerCache.forEach((uuid, player) -> {
            player.save();
        });
        CobbledJobsFabric.dataWrapper.playerCache.clear();
    }

    public Player getPlayerFlatFile(UUID uuid) {
        if (CobbledJobsFabric.dataWrapper.playerCache.containsKey(uuid))
            return CobbledJobsFabric.dataWrapper.playerCache.get(uuid);

        File dir = CobbledJobsFabric.getPlayerStorageDir();
        dir.mkdirs();


        File dataFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(dataFile));
        } catch (FileNotFoundException e) {
            CobbledJobsFabric.getLog().error("Something went wrong attempting to read the Player Data, new Player Perhaps?");
            return null;
        }
        return gson.fromJson(reader, Player.class);


    }

    public Player getPlayer(UUID uuid) {
        if (CobbledJobsFabric.dataWrapper.playerCache.containsKey(uuid))
            return CobbledJobsFabric.dataWrapper.playerCache.get(uuid);
        if (CobbledJobsFabric.dbConfig.enabled) {
            if (this.database != null) {
                return this.database.getPlayer(uuid);
            }
        }

        File dir = CobbledJobsFabric.getPlayerStorageDir();
        dir.mkdirs();


        File dataFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(dataFile));
        } catch (FileNotFoundException e) {
            CobbledJobsFabric.getLog().error("Something went wrong attempting to read the Player Data, new Player Perhaps?");
            return null;
        }
        return gson.fromJson(reader, Player.class);


    }

    public void savePlayer(Player player) {
        if (CobbledJobsFabric.dbConfig.enabled) {
            //save to db
            if (this.database != null)
            {
                this.database.save(player);
            }
        } else {

            File dir = CobbledJobsFabric.getPlayerStorageDir();
            dir.mkdirs();

            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.uuid)));
            Gson gson = Adapters.PRETTY_MAIN_GSON;
            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (reader == null) {
                CobbledJobsFabric.getLog().error("Something went wrong attempting to read the Player Data");
                return;
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(gson.toJson(player));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        player.updateCache();
    }

    public void savePlayerNoCache(Player player) {
        if (CobbledJobsFabric.dbConfig.enabled) {
            if (this.database != null)
            {
                this.database.save(player);
            }
        } else {

            File dir = CobbledJobsFabric.getPlayerStorageDir();
            dir.mkdirs();

            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.uuid)));
            Gson gson = Adapters.PRETTY_MAIN_GSON;
            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (reader == null) {
                CobbledJobsFabric.getLog().error("Something went wrong attempting to read the Player Data");
                return;
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(gson.toJson(this));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void makePlayer(ServerPlayer player) {

        Player dexPlayer = new Player(player.getUUID());
        if (CobbledJobsFabric.dbConfig.enabled)
        {
            if (this.database != null)
            {
                this.database.makePlayer(player.getUUID());
            }
        } else {
            File dir = CobbledJobsFabric.getPlayerStorageDir();
            dir.mkdirs();


            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.getUUID())));
            if (file.exists()) {
                CobbledJobsFabric.getLog().error("There was an issue generating the Player, Player already exists? Ending function");
                return;
            }

            Gson gson = Adapters.PRETTY_MAIN_GSON;
            String json = gson.toJson(dexPlayer);

            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public List<Player> getAllPlayersFromFiles(boolean database)
    {

        List<UUID> addedPlayers = new ArrayList<>();

        List<Player> playerList = new ArrayList<>();


        File dir = CobbledJobsFabric.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    continue;
                }
                if (addedPlayers.contains(uuid))
                    continue;
                Player p;
                if (database)
                    p = getPlayer(uuid);
                else p = getPlayerFlatFile(uuid);
                if (p == null) {
                    CobbledJobsFabric.getLog().warn("Failed retrieving data for %uuid%".replace("%uuid%", uuid.toString()));
                    continue;
                }
                playerList.add(p);
                addedPlayers.add(uuid);
            }
        }

        return playerList;
    }
}
