package org.greenskye.wurmunlimited.mods.surfacemining;

import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.players.Player;
import org.greenskye.wurmunlimited.mods.surfacemining.onetilemining;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import java.util.List;
import java.util.Arrays;

public class onetilereloadaction implements WurmServerMod, ItemTypes, MiscConstants, ModAction, BehaviourProvider, ActionPerformer {
	public static short actionId;
	static ActionEntry actionEntry;

	public onetilereloadaction() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(actionId, "Reload org.greenskye.wurmunlimited.mods.surfacemining properties", "Reloading org.greenskye.wurmunlimited.mods.surfacemining.onetilemining properties", new int[]{
				}); 
		ModActions.registerAction(actionEntry);
	}

	@Override
	public BehaviourProvider getBehaviourProvider() {
		return this;
	}

	@Override
	public ActionPerformer getActionPerformer() {
		return this;
	}

	@Override
	public short getActionId() {
		return actionId;
	}
	
	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
		return getBehavioursFor(performer, target);
	}
	
	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
		if (performer instanceof Player && performer.getPower() > 4) {
				return (List<ActionEntry>) Arrays.asList(actionEntry);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
		return action(act, performer, target, action, counter);
	}
	
	@Override
	public boolean action(Action act, Creature performer, Item target, short action, float counter) {
		if (performer instanceof Player && performer.getPower() > 4) {
			Path path = Paths.get("mods/org.greenskye.wurmunlimited.mods.surfacemining.onetilemining.properties");
			
	        if (!Files.exists(path)) {
	            performer.getCommunicator().sendAlertServerMessage("The config file seems to be missing.");
	            return true;
	        }			
	        InputStream stream = null;
	        try {
	            performer.getCommunicator().sendAlertServerMessage("Opening the config file.");
	            stream = Files.newInputStream(path);
	            Properties properties = new Properties();
	            
	            performer.getCommunicator().sendAlertServerMessage("Reading from the config file.");
	            properties.load(stream);
	            
	            onetilemining.doconfig(properties);
	           // logger.info("Reloading configuration.");
	            performer.getCommunicator().sendAlertServerMessage("Loading all options.");

	        }
	        catch (Exception ex) {
	            //logger.log(Level.SEVERE, "Error while reloading properties file.", ex);
	            performer.getCommunicator().sendAlertServerMessage("Error reloading the config file, check the server log.");
	        }
	        finally {
	            try {
	                if (stream != null)
	                    stream.close();
	            }
	            catch (Exception ex) {
	               // logger.log(Level.SEVERE, "Properties file not closed, possible file lock.", ex);
	                performer.getCommunicator().sendAlertServerMessage("Error closing the config file, possible file lock.");
	            }
	        }			
		}
		return true;
	}
}