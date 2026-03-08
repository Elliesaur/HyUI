package au.ellie.hyui.commands;

import au.ellie.hyui.utils.PngDownloadUtils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;


public class ReplicateCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> urlArg;

    public ReplicateCommand() {
        super("tfetch", "Test image prefetch");
        this.urlArg = withRequiredArg("url", "Image url to prefetch", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            context.sendMessage(Message.raw("Error: Could not get player"));
            return;
        }
        String imageUrl;
        String url = urlArg.get(context);
        if (url.equals("1")) {
            imageUrl = "https://dummyimage.com/450.png";
        } else if (url.equals("2")) {
            imageUrl = "https://dummyimage.com/500.png";
        } else if (url.equals("3")) {
            imageUrl = "https://dummyimage.com/550.png";
        } else {
            imageUrl = null;
        }
        world.execute(() -> {
            try {
                PngDownloadUtils.CachedAssetInfo newCacheEntry = PngDownloadUtils.prefetchPngForPlayer(playerRef, imageUrl, 12000);
                System.out.println("Cache entry: " + newCacheEntry.toString()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}