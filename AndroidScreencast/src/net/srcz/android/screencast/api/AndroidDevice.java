package net.srcz.android.screencast.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import net.srcz.android.screencast.api.file.FileInfo;
import net.srcz.android.screencast.api.injector.NullSyncProgressMonitor;
import net.srcz.android.screencast.api.injector.OutputStreamShellOutputReceiver;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncService.ISyncProgressMonitor;

public class AndroidDevice {

	IDevice device;

	public AndroidDevice(IDevice device) {
		this.device = device;
	}

	public void openUrl(String url) {
		executeCommand("am start -a android.intent.action.VIEW -d "+url);
		//zhudm add for test, lauch calculator	
		//executeCommand("am start -n com.android.calculator2/com.android.calculator2.Calculator");
		//executeCommand("input keyevent 3");
		 System.out.println("url = "+url); 
	}
	
	public String executeCommand(String cmd) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			device.executeShellCommand(cmd,
					new OutputStreamShellOutputReceiver(bos));
			return new String(bos.toByteArray(), "UTF-8");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void pushFile(File localFrom, String remoteTo) {
		try {
			if (device.getSyncService() == null)
				throw new RuntimeException("SyncService is null, ADB crashed ?");

			device.getSyncService().pushFile(localFrom.getAbsolutePath(),
					remoteTo, new NullSyncProgressMonitor());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void pullFile(String removeFrom, File localTo) {
		// ugly hack to call the method without FileEntry
		try {
			if (device.getSyncService() == null)
				throw new RuntimeException("SyncService is null, ADB crashed ?");

			Method m = device.getSyncService().getClass().getDeclaredMethod(
					"doPullFile", String.class, String.class,
					ISyncProgressMonitor.class);
			m.setAccessible(true);
			m.invoke(device.getSyncService(), removeFrom, localTo.getAbsolutePath(), device
					.getSyncService().getNullProgressMonitor());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	//zhudm add	
	public void sendVKey(int keycode) {
		//System.out.println("sendVKey = "+keycode); 
		executeCommand("input keyevent "+keycode);
		//executeCommand("am start -n com.android.calculator2/com.android.calculator2.Calculator");
	}	
	public void sendTouchTap(int x, int y) {
		System.out.println("sendTouchTap = "+x+' '+y); 
		executeCommand("input touchscreen tap "+x+' '+y);
	}
	public void unLock() {
		System.out.println("unlock screen"); 
		executeCommand("input touchscreen swipe 249 662 448 662 300");
	}

	public void swapRight() {
		System.out.println("swap right"); 
		executeCommand("input touchscreen swipe 448 662 100 662 300");
	}	
	
	public void swapLeft() {
		System.out.println("swap left"); 
		executeCommand("input touchscreen swipe 100 662 448 662 300");
	}
	public void touchUp() {
		System.out.println("touch Up"); 
		executeCommand("input touchscreen swipe 373 555 373 404 300");
	}
	public void touchDown() {
		System.out.println("touch Down"); 
		executeCommand("input touchscreen swipe 373 404 373 555 300");
	}

	public List<FileInfo> list(String path) {
		try {
			String s = executeCommand("ls -l "+path);
			String[] entries = s.split("\r\n");
			Vector<FileInfo> liste = new Vector<FileInfo>();
			for (int i = 0; i < entries.length; i++) {
				String[] data = entries[i].split(" ");
				if (data.length < 4)
					continue;
				/*
				 * for(int j=0; j<data.length; j++) {
				 * System.out.println(j+" = "+data[j]); }
				 */
				String attribs = data[0];
				boolean directory = attribs.startsWith("d");
				String name = data[data.length - 1];

				FileInfo fi = new FileInfo();
				fi.attribs = attribs;
				fi.directory = directory;
				fi.name = name;
				fi.path = path;
				fi.device = this;

				liste.add(fi);
			}

			return liste;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
