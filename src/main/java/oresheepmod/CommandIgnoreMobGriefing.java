package oresheepmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandIgnoreMobGriefing extends CommandBase
{
	public List getCommandAliases()
	{
		List<String> aliases = new ArrayList();
		aliases.add("ignoreRuleGriefing");
		aliases.add("ignoreGameRuleGriefing");
		aliases.add("ignoreMobGriefing");
		return aliases;
	}
	
	 /**
     * Gets the name of the command
     */
    public String getName() 
    { 
        return "ignoreGameRuleMobGriefing"; 
    }
    
    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    /**
     * Gets the usage string for the command.
     */
    public String getUsage(ICommandSender sender)
    {
        return "commands.oresheepmod.ignoreGameRuleMobGriefing.usage";
    }

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length <= 0 || args.length > 1)
        {
            throw new WrongUsageException("commands.oresheepmod.ignoreGameRuleMobGriefing.usage", new Object[0]);
        }
		else if (args[0].length() <= 0 && !args[0].equals("true") && !args[0].equals("false"))
		{
            throw new WrongUsageException("commands.oresheepmod.ignoreGameRuleMobGriefing.invalid", new Object[0]);
		}
		else
		{
        	if (args[0].equals("true"))
        	{
    		    ModConfig.IgnoreGameRuleMobGriefing = true;
        	}
        	else if (args[0].equals("false"))
        	{
    		    ModConfig.IgnoreGameRuleMobGriefing = false;
        	}
        	notifyCommandListener(sender, this, 1, "commands.oresheepmod.ignoreGameRuleMobGriefing.valid", args[0]);
		}
	}

    /**
     * Get a list of options for when the user presses the TAB key
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"true", "false"});
        }
        return Collections.<String>emptyList();
    }
}

