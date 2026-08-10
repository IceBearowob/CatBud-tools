package ice.catbudtools.client.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatScreen.class)
public interface ChatScreenAccessor {
    @Accessor("chatInputSuggestor")
    ChatInputSuggestor catbud$getChatInputSuggestor();

    @Accessor("chatField")
    TextFieldWidget catbud$getChatField();
}
