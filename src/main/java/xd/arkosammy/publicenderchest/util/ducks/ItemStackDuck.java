package xd.arkosammy.publicenderchest.util.ducks;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public interface ItemStackDuck {

    void publicenderchest$setInserterName(Text name);

    @Nullable
    Text publicenderchest$getInserterName();

    void publicenderchest$setInsertedTime(LocalDateTime time);

    @Nullable
    LocalDateTime publicenderchest$getInsertedTime();

}
