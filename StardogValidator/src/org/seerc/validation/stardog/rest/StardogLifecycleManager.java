package org.seerc.validation.stardog.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.complexible.common.protocols.server.Server;
import com.complexible.common.protocols.server.ServerException;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;
import com.google.common.io.Files;

@WebListener
public class StardogLifecycleManager implements ServletContextListener {
	
	Server aServer = null;
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		InputStream licenseKeyIs = getClass().getClassLoader().getResourceAsStream("../resources/stardog-license-key.bin");
		OutputStream outputStream = null;
		// write the inputStream to a FileOutputStream
		try {
			outputStream = 
			            new FileOutputStream(new File("stardog-license-key.bin"));
			int read = 0;
			byte[] bytes = new byte[1024];
	
			while ((read = licenseKeyIs.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			aServer = Stardog
					.buildServer()
					.bind(SNARLProtocolConstants.EMBEDDED_ADDRESS)
					.start();
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		aServer.stop();
	}

}
