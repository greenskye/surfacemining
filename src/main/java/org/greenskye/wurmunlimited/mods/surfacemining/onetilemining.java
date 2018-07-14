package org.greenskye.wurmunlimited.mods.surfacemining;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.items.Item;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;



public class onetilemining implements WurmServerMod, Configurable, Initable, MiscConstants, ServerStartedListener {
	static Logger logger = Logger.getLogger(onetilemining.class.getName());
	static int onetileminingline = 3186;
	static int onetileminingline2 = 3451;
	static int count = 0;
	static int countskill = 0;
	static boolean done = false;
	static float levelfactor = 1.0f;
	static String minecode = "Math.max(0.5f, (float)mining.getKnowledge(0.0) / 100.0f);";
	static boolean moonmetalimp = false;
	static boolean onetile = false;

	@Override
	public void init() {
		replacemethod();
		replacemethod2();
		replacemethod3();
		if (moonmetalimp) moonhook();
	}
	public static void doconfig(Properties properties){
		minecode = properties.getProperty("minecode",minecode);
		levelfactor = Float.parseFloat(properties.getProperty("levelfactor", Float.toString(levelfactor)));
		moonmetalimp = Boolean.parseBoolean(properties.getProperty("moonmetalimp", Boolean.toString(moonmetalimp)));
		onetile = Boolean.parseBoolean(properties.getProperty("onetilemining", Boolean.toString(onetile)));

	}
	@Override
	public void onServerStarted() {
		ModActions.registerAction(new onetilereloadaction());
	}

	public void configure(Properties properties) {
		doconfig(properties);

	}

	private void moonhook(){
		//getTemplateIdForMaterial

		try {
			ClassPool classPool = HookManager.getInstance().getClassPool();
			String descriptor = Descriptor.ofMethod(CtPrimitiveType.intType, new CtClass[] {
					classPool.get("com.wurmonline.server.items.Item")});
			HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.MethodsItems", "getImproveTemplateId", descriptor, new InvocationHandlerFactory(){

				@Override
				public InvocationHandler createInvocationHandler(){
					return new InvocationHandler(){
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							//logger.log(Level.INFO, "Moon metal imp using steel hook");
							byte material = MethodsItems.getImproveMaterial((Item) args[0]);
							switch (material) {
								case 56: {
									return 205;
								}
								case 57: {
									return 205;
								}
								case 67: {
									return 205;
								}
							}
							return method.invoke(proxy, args);
						};
					};
				}
			});
		}
		catch (Exception e) {
			throw new HookException(e);
		}
	}

	private void replacemethod(){
		count = 0;
		if(onetile == true) {
			try {
				CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.TileRockBehaviour");
				ctc.getDeclaredMethod("mine").instrument(new ExprEditor(){
					public void edit(MethodCall m) throws CannotCompileException {
						if (m.getMethodName().equals("getTile")) {
							count = count +1;
							if (count == 2) {
								m.replace("$_ = org.greenskye.wurmunlimited.mods.surfacemining.onetilecheck.canmine(digTilex,digTiley);");
								return;
							}
						}
					}

				});
			}
			catch (CannotCompileException | NotFoundException e) {
				logger.log(Level.SEVERE, "Failed to apply onetilemining interception", (Throwable)e);
			}
		}

		countskill = 0;
		try {
			CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.TileRockBehaviour");
			ctc.getDeclaredMethod("mine").instrument(new ExprEditor(){
				public void edit(MethodCall m) throws CannotCompileException {
					if (m.getMethodName().equals("max")) {
						countskill = countskill +1;
						logger.log(Level.INFO, countskill+" count");
						if (countskill == 3) {
							logger.log(Level.INFO, "Replacing Math.max(0.2f, (float)mining.getKnowledge(0.0) / 200.0f);");
							m.replace("$_ = "+minecode);
							return;
						}
					}
				}

			});
		}
		catch (CannotCompileException | NotFoundException e) {
			logger.log(Level.SEVERE, "Failed to apply onetilemining interception", (Throwable)e);
		}



	}


	private void replacemethod2(){
		try {
			CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.TileRockBehaviour");
			ctc.getDeclaredMethod("mine").instrument(new ExprEditor(){
				public void edit(MethodCall m) throws CannotCompileException {
					if (m.getMethodName().equals("encode") && done == false){
						done = true;
						m.replace("$_ = org.greenskye.wurmunlimited.mods.surfacemining.onetilecheck.gettile(digTilex,digTiley,newHeight);");
						return;
					}
				}

			});
		}
		catch (CannotCompileException | NotFoundException e) {
			logger.log(Level.SEVERE, "Failed to apply onetilemining interception", (Throwable)e);
		}
	}
	private void replacemethod3(){
		try {
			CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.CaveTileBehaviour");
			ctc.getDeclaredMethod("flatten").instrument(new ExprEditor(){
				int thiscount = 0;
				public void edit(MethodCall m) throws CannotCompileException {
					if (m.getMethodName().equals("getStandardActionTime")){
						thiscount = thiscount +1;
						if (thiscount == 1 ||  thiscount == 3  || thiscount == 5 ) {
							m.replace("$_ = org.greenskye.wurmunlimited.mods.surfacemining.onetilecheck.getactiontime(performer, mining, source);");
							return;
						}
					}
				}

			});
		}
		catch (CannotCompileException | NotFoundException e) {
			logger.log(Level.SEVERE, "Failed to apply level interception", (Throwable)e);
		}

	}

}

