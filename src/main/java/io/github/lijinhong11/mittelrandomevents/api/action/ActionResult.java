package io.github.lijinhong11.mittelrandomevents.api.action;

import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittelrandomevents.MittelRandomEvents;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class ActionResult {
    @Getter
    private final boolean isSuccessful;

    private final @NotNull Function<@Nullable Player, @Nullable Component> message;

    public static ActionResult success() {
        return new ActionResult(true, (p) -> null);
    }

    public static ActionResult fail(Function<@Nullable Player, @Nullable Component> message) {
        return new ActionResult(false, message);
    }

    public static ActionResult fail(Component message) {
        return new ActionResult(false, player -> message);
    }

    public static ActionResult valueNotPresented(String parameterName) {
        return new ActionResult(false, p -> MittelRandomEvents.getInstance()
                .getLanguageManager()
                .getMsgComponent(
                        p,
                        "mre.action.error.value_not_presented",
                        MessageReplacement.replace("%parameter%", parameterName)));
    }

    public static ActionResult invalidValue(String parameterName, String value) {
        return fail(p -> MittelRandomEvents.getInstance()
                .getLanguageManager()
                .getMsgComponent(
                        p,
                        "mre.action.error.invalid_value",
                        MessageReplacement.replace("%parameter%", parameterName),
                        MessageReplacement.replace("%value%", value)));
    }

    public static ActionResult intRange(String parameterName, int minimum, int maximum) {
        return numberRange("int_range", parameterName, minimum, maximum);
    }

    public static ActionResult longRange(String parameterName, long minimum, long maximum) {
        return numberRange("long_range", parameterName, minimum, maximum);
    }

    public static ActionResult floatRange(String parameterName, float minimum, float maximum) {
        return numberRange("float_range", parameterName, minimum, maximum);
    }

    public static ActionResult doubleRange(String parameterName, double minimum, double maximum) {
        return numberRange("double_range", parameterName, minimum, maximum);
    }

    private static ActionResult numberRange(String key, String parameterName, Number minimum, Number maximum) {
        return fail(p -> MittelRandomEvents.getInstance()
                .getLanguageManager()
                .getMsgComponent(
                        p,
                        "mre.action.error." + key,
                        MessageReplacement.replace("%parameter%", parameterName),
                        MessageReplacement.replace("%min%", minimum.toString()),
                        MessageReplacement.replace("%max%", maximum.toString())));
    }

    public void sendMessage(@NotNull CommandSender recipient) {
        if (isSuccessful) {
            return;
        }
        Component rendered = message.apply(recipient instanceof Player player ? player : null);
        if (rendered != null) {
            recipient.sendMessage(rendered);
        }
    }
}
