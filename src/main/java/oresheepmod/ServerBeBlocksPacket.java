package oresheepmod;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

//sends beblock array
public class ServerBeBlocksPacket implements IMessage 
{
	public ServerBeBlocksPacket(){}

	ArrayList<Integer> beBlockInts = new ArrayList<Integer>();
	ArrayList<Integer> eatBlockInts = new ArrayList<Integer>();
	int lengthBe = 0;
	int lengthEat = 0;
	public ServerBeBlocksPacket(ArrayList<BlockEntry> beBlocks, ArrayList<Block> eatBlocks, boolean ignoreGameRuleboolean) 
	{
		//integers are stored the same way an ore sheep stores an integer, by block id + meta
		for (int i = 0; i < beBlocks.size(); i++)
		{
			BlockEntry k = beBlocks.get(i);
			int value = (k.getBlock().getIdFromBlock(k.getBlock())* 100) + k.getMeta();
			beBlockInts.add(value);
		}
		//size of the array
		lengthBe = beBlockInts.size();
		for (Block k : eatBlocks)
		{
			int value =  k.getIdFromBlock(k);
			eatBlockInts.add(value);
		}
		lengthEat = eatBlockInts.size();
	}

	@Override 
	public void toBytes(ByteBuf buf) 
	{
		buf.writeInt(lengthBe);
		for (int k : beBlockInts)
		{
			buf.writeInt(k);
		}
		buf.writeInt(lengthEat);
		for (int k : eatBlockInts)
		{
			buf.writeInt(k);
		}
	}

	@Override 
	public void fromBytes(ByteBuf buf)
	{
		// Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
		beBlockInts = new ArrayList<Integer>();
		int lengthBe = buf.readInt();
		for (int i = 0; i < lengthBe; i++)
		{
			beBlockInts.add(buf.readInt());
		}
		eatBlockInts = new ArrayList<Integer>();
		int lengthEat = buf.readInt();
		for (int i = 0; i < lengthEat; i++)
		{
			eatBlockInts.add(buf.readInt());
		}
	}
	
	public ArrayList<Integer> getBeToSend()
	{
		return beBlockInts;
	}
	
	public ArrayList<Integer> getEatToSend()
	{
		return eatBlockInts;
	}
	
	//this is what ServerBeBlocksPacket does to the client (output comes from client)
	//The params of the IMessageHandler are <REQ, REPLY>, meaning that the first is the packet you are receiving, and the second is the packet you are returning. The returned packet can be used as a "response" from a sent packet.
	public static class BeBlocksHandler implements IMessageHandler<ServerBeBlocksPacket, IMessage> 
	{
		@Override
		public IMessage onMessage(ServerBeBlocksPacket message, MessageContext ctx)
		{
			ArrayList<Integer> beInts = message.getBeToSend();
			ArrayList<BlockEntry> beBlocks = new ArrayList<BlockEntry>();
			for (Integer i : beInts)
			{
				Block block = Block.getBlockById((int)((double)i / 100));
				if (!OreRegistryDraw.isBlockLegal(block))
				{
					continue;
				}
				byte met = (byte) (i % 100);
				beBlocks.add(new BlockEntry(block.getStateFromMeta(met)));
			}
			OreRegistryDraw.setBeBlocks(beBlocks);
			
			ArrayList<Integer> eatInts = message.getEatToSend();
			ArrayList<Block> eatBlocks = new ArrayList<Block>();
			for (Integer i : eatInts)
			{
				Block block = Block.getBlockById(i);
				if (!OreRegistryDraw.isBlockLegal(block))
				{
					continue;
				}
				eatBlocks.add(block);
			}
			//TODO also needs to check metadata values
			OreRegistryDraw.setEatBlocks(eatBlocks);
			//response packet
			return null;
		}
	}
}
