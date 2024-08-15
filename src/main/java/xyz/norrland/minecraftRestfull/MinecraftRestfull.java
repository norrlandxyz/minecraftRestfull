package xyz.norrland.minecraftRestfull;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;

import net.milkbowl.vault.permission.Permission;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MinecraftRestfull extends JavaPlugin {

    //vault api
    private static Permission perms = null;

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        final String AUTH_TOKEN = this.getConfig().getString("auth-token");

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
                .start(7070);

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
        web.get(prefix+"/bukkit/dispatchCommand/{command}", ctx -> {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            String command = ctx.pathParam("command");
            AtomicBoolean success = new AtomicBoolean(false);
            Bukkit.getScheduler().runTask(this, () -> {
                success.set(Bukkit.dispatchCommand(console, command));
            });
            ctx.json(true);
        });
        //---------------------------------------------------------------

        getLogger().info("Started webserver on localhost:7070");
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
