/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.side;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.simpleframework.http.Part;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;


/**
 * loads a model trained using lightSIDE uses it to label new instances via the
 * web. TODO (maybe): allow classification of multiple instances at once, or by
 * multiple classifiers, upload new trained models (possible?)
 * 
 * @author dadamson
 */
public class PredictionServer implements Container
{
	private static Map<String, Predictor> predictors = new HashMap<String, Predictor>();

	private final Executor executor;

	public static void serve(int port, int threads) throws Exception
	{
		Container container = new PredictionServer(threads);

		Server server = new ContainerServer(container);
		Connection connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(port);

		connection.connect(address);
		System.out.println("Started server on port " + port + ".");
	}

	public void handle(final Request request, final Response response)
	{
		executor.execute(new Runnable()
		{

			@Override
			public void run()
			{
				handleRequest(request, response);
			}
			
		});
	}

	public void handleRequest(Request request, Response response)
	{
		try
		{
			PrintStream body = response.getPrintStream();
			long time = System.currentTimeMillis();

			String target = request.getTarget();
			System.out.println(request.getMethod() + ": " + target);

			String answer = null;

			response.setValue("Content-Type", "text/plain");
			response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
			response.setDate("Date", time);
			response.setDate("Last-Modified", time);

			if (target.equals("/upload"))
			{
				if (request.getMethod().equals("POST"))
				{
					answer = handleUpload(request, response);
				}
				else
				{
					answer = handleGetUpload(request, response);
				}

			}
			else if (target.startsWith("/predict"))
			{
				answer = handlePredict(request, response);
			}
			else if (target.startsWith("/favicon.ico"))
			{
				answer = handleIcon(request, response, body);
			}

			if (answer == null)
			{
				response.setCode(404);
				body.println("There is no data, only zuul.");
			}
			else
				body.println(answer);

			int code = response.getCode();
			if (code != 200)
			{
				body.println("HTTP Code " + code);
				System.out.println("HTTP Code " + code);
			}

			body.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String handleIcon(Request request, Response response, PrintStream out)
	{
		try
		{
			File icon = new File("cfl.ico");
			response.setDate("Last-Modified", icon.lastModified());
			response.setValue("Content-Type", "image/ico");
			FileInputStream in = new FileInputStream("cfl.ico");
			
			byte[] buffer = new byte[1024];
			int len = in.read(buffer);
			while (len != -1) {
			    out.write(buffer, 0, len);
			    len = in.read(buffer);
			    if (Thread.interrupted()) {
			        throw new InterruptedException();
			    }
			}
			return "";
		}
		catch(Exception e)
		{
			response.setCode(500);
			return "Ack!";
		}
			
	}

	private String handleGetUpload(Request request, Response response)
	{
		response.setValue("Content-Type", "text/html");
		return "<head><title>SIDE Loader</title></head><body>" + "<h1>SIDE Loader</h2>"
				+ "<form action=\"upload\" method=\"post\" enctype=\"multipart/form-data\">" + "Serialized Model: <input type=\"file\" name=\"model\"><br>"
				+ "Model Nickname:<input type=\"text\" name=\"modelNick\"> " + "<input type=\"submit\" name=\"Submit\" value=\"Upload Model\">" + "</form>"
				+ "</body>";
	}

	/**
	 * @param request
	 * @param body
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected String handleUpload(Request request, Response response) throws IOException, FileNotFoundException
	{
		Part part = request.getPart("model");
		String nick = request.getPart("modelNick").getContent();
		String path = part.getFileName();
		// TODO: use threaded tasks.

		if (part != null && part.getFileName() != null)
		{
			if (path != null && !path.isEmpty() && (nick == null || nick.isEmpty()))
			{
				nick = path.replace(".model.side", "");
			}

			if (nick.isEmpty() || path.equals("null"))
			{
				response.setCode(400);
				return "upload requires both serialized model file and model name.";
			}
			nick = nick.replaceAll("/", "-");
			nick = nick.replaceAll("\\s", "-");

			if (predictors.containsKey(nick))
			{
				response.setCode(409);
				return "nickname '" + nick + "'is already in use for a model at " + predictors.get(nick).getModelPath();
			}

			File f = new File("saved/" + nick + ".model.side");
			if (f.exists())
			{
				response.setCode(409);
				return nick + " already exists on this server.";
			}
			else
			{
				// TODO: authentication?
				FileChannel fo = new FileOutputStream(f).getChannel();
				ReadableByteChannel po = Channels.newChannel(part.getInputStream());
				long transferred = fo.transferFrom(po, 0, Integer.MAX_VALUE);
				System.out.println("wrote " + transferred + " bytes.");

				boolean attached = attachModel(nick, f.getAbsolutePath());

				if (attached)
					return "received " + path + ": " + transferred + " bytes.\nModel attached as /predict/" + nick + "";
				else
				{
					f.delete();
					response.setCode(418);
					return "could not attach model '" + path + "' -- was it trained on the latest version of LightSIDE?";
				}
			}
		}
		else
		{
			response.setCode(400);
			return "No model file received.";
		}
	}

	public PredictionServer(int size)
	{
		this.executor = Executors.newFixedThreadPool(size);
	}

	protected String handlePredict(Request request, Response response) throws IOException
	{
		// TODO: use threaded tasks.
		String answer = "";
		String model = "";

		try
		{

			Query query = request.getQuery();

			List<String> instances = query.getAll("q");

			model = request.getPath().getPath(1).substring(1);

			if (!predictors.containsKey(model))
			{
				File f = new File("saved/" + model + ".model.side");// attempt to
																// attach a
																// local model
				if (f.exists())
				{
					boolean attached = attachModel(model, f.getAbsolutePath());
					if (!attached)
					{
						response.setCode(418);
						return "could not load existing model for '" + model + "' -- was it trained on the latest version of LightSIDE?";
					}
				}
				else
				{
					response.setCode(404);
					return "no model available at predict/" + model;
				}
			}

			System.out.println("using model " + model + " on " + instances.size() + " instances...");
			for (Comparable label : predictors.get(model).predict(instances))
			{
				answer += label + " ";
			}
			answer = answer.trim();
			
			if(answer.isEmpty())
				response.setCode(500);
			
			return answer;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			response.setCode(400);
			return "could not handle request: " + request.getTarget() + "\n(urls should be of the form /predict/model/?q=instance...)";
		}
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			printUsage();
		}

		initSIDE();
		int port = 8000;

		int start = 0;
		if (args.length > 0 && !args[0].contains(":"))
		{
			try
			{
				start = 1;
				port = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				printUsage();
			}
		}

		for (int i = start; i < args.length; i++)
		{

			String[] modelConfig = args[i].split(":");
			String modelNick = modelConfig[0];
			String modelPath = modelConfig[1];

			attachModel(modelNick, modelPath);
		}

		if (predictors.isEmpty())
		{
			System.out.println("Warning: no models attached yet. Use http://localhost:" + port + "/upload");
		}

		serve(port, 5);

	}

	/**
	 * 
	 */
	protected static void printUsage()
	{
		System.out.println("usage: side_server.sh [port] model_nickname:path/to/model.side ...");
	}

	/**
	 * @param modelPath
	 * @param annotation
	 * @param annotation2
	 * @return
	 */
	protected static boolean attachModel(String nick, String modelPath)
	{
		try
		{
			System.out.println("attaching " + modelPath + " as " + nick);
			Predictor predict = new Predictor(modelPath, "class");
			predictors.put(nick, predict);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 */
	protected static void initSIDE()
	{
//		SIDEPlugin[] fex = PluginManager.getSIDEPluginArrayByType("feature_hit_extractor");
//		for (int k = 0; k < fex.length; k++)
//		{
//			FeaturePlugin plug = (FeaturePlugin) fex[k];
//			System.out.println(plug.getOutputName());
//		}
	}

}
