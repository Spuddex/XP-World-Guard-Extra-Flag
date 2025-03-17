package Spuddex.XpGatekeep.flags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

/**
 * Flag that stores an XP level requirement for a region
 */
public class XpLevelFlag extends Flag<Integer> {

    public XpLevelFlag(final String name) {
        super(name);
    }

    @Override
    public Integer parseInput(final FlagContext context) throws InvalidFlagFormat {
        final String input = context.getUserInput();
        try {
            final int level = Integer.parseInt(input);
            if (level < 0) {
                throw new InvalidFlagFormat("XP level must be a positive number");
            }
            return level;
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Expected a number for XP level");
        }
    }

    @Override
    public Integer unmarshal(final Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Object marshal(final Integer o) {
        return o;
    }
}