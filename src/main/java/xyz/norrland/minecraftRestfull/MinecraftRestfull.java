package xyz.norrland.minecraftRestfull;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;

import net.milkbowl.vault.permission.Permission;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public final class MinecraftRestfull extends JavaPlugin {

    //vault api
    private static Permission perms = null;

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        final String AUTH_TOKEN = this.getConfig().getString("auth-token");
        final int PORT = this.getConfig().getInt("webserver-port");

        //vault api
        setupPermissions();

        //WEB ---------------------------------------------------------------
        String prefix = "/_minecraftRestfull/";

        var web = Javalin.create()
                .before(ctx -> {
                    String token = ctx.header("auth-token");
                    if (token == null || !token.equals(AUTH_TOKEN)) {
                        throw new UnauthorizedResponse();
                    }
                })
                .start(PORT);

        //VAULT ENDPOINTS ---------------------------------------------------------------
        web.get(prefix+"/vault/getPlayerGroups/{player_uuid}", ctx -> {
            String player_uuid = ctx.pathParam("player_uuid");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));
            String[] player_perms = perms.getPlayerGroups(null, player);
            ctx.json(player_perms);
        });

        web.get(prefix+"/vault/playerAddGroup/{player_uuid}/{group_name}", ctx -> {
            String player_uuid = ctx.pathParam("player_uuid");
            String group_name = ctx.pathParam("group_name");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));
            boolean success = perms.playerAddGroup(null, player, group_name);
            ctx.json(Boolean.toString(success));
        });

        web.get(prefix+"/vault/playerRemoveGroup/{player_uuid}/{group_name}", ctx -> {
            String player_uuid = ctx.pathParam("player_uuid");
            String group_name = ctx.pathParam("group_name");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));
            boolean success = perms.playerRemoveGroup(null, player, group_name);
            ctx.json(Boolean.toString(success));
        });

        //---------------------------------------------------------------

        //BUKKIT ENDPOINTS ---------------------------------------------------------------
        web.get(prefix+"/bukkit/dispatchCommand", ctx -> {

            //instead of url encoding (see readme)
            String command = ctx.body();

            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.dispatchCommand(console, command);
            });

            ctx.json(true);
        });

        web.get(prefix+"/bukkit/setWhitelisted/true/{player_uuid}", ctx -> {
            String player_uuid = ctx.pathParam("player_uuid");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));

            boolean response = false;

            if(!player.isWhitelisted()) {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.setWhitelisted(true);
                });
                response = true;
            }
            ctx.json(response);
        });

        web.get(prefix+"/bukkit/setWhitelisted/false/{player_uuid}", ctx -> {
            String player_uuid = ctx.pathParam("player_uuid");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));

            boolean response = false;

            if(player.isWhitelisted()) {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.setWhitelisted(false);
                });
                response = true;
            }
            ctx.json(response);
        });

        web.get(prefix+"/bukkit/getWhitelistedPlayers", ctx -> {

            Set<OfflinePlayer> whitelistedPlayers = Bukkit.getWhitelistedPlayers();
            ArrayList<String> uuids = new ArrayList<>();
            for(OfflinePlayer player : whitelistedPlayers) {
                uuids.add(player.getUniqueId().toString());
            }
            ctx.json(uuids);
        });

        web.get(prefix+"/bukkit/isWhitelisted/{player_uuid}", ctx -> {

            String player_uuid = ctx.pathParam("player_uuid");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));

            ctx.json(player.isWhitelisted());
        });
        //---------------------------------------------------------------

        getLogger().info("Started webserver on localhost:"+PORT);
        //------------------------------------------------------------------
    }

    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    //vault api ------------------------------------------------------------------
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    public static Permission getPermissions() {
        return perms;
    }
    // -----------------------------------------------------------------------------
}
