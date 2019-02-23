package edu.neu.csye6220.assignment3;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Servlet implementation class WeatherForecast
 */
@WebServlet("/WeatherForecast")
public class WeatherForecast extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WeatherForecast() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String city = "";
		String language = "en";
		String standard = "imperial";
		int rows = 5;
		int days = Integer.parseInt(request.getParameter("rows"));
		if (request.getParameter("txtCity") != null && request.getParameter("txtCity").trim() != "") {
			city = URLEncoder.encode(request.getParameter("txtCity").split(",")[0],"UTF-8");//.split(",")[0];
			
		}

		try {
			String weatherXml = getForecast(city,language,standard,rows);
			Weather wResult =	readXmlForecast(weatherXml,days);
			if(wResult!=null) {
			HttpSession weathSess = request.getSession();
			weathSess.setAttribute("wer", wResult);
			response.sendRedirect("WeatherForecast.jsp");
			}
			else {
				response.sendRedirect("Error.jsp");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Weather readXmlForecast(String weatherContent, int days) {

		
		Weather wObj = new Weather();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(weatherContent)));
			document.getDocumentElement().normalize();

			NodeList nameList = document.getElementsByTagName("name");
			if(nameList!=null && nameList.getLength()>0) {
			wObj.setLocation(nameList.item(0).getTextContent());
			NodeList timeList = document.getElementsByTagName("time");
			
			for (int i = 0; i < timeList.getLength(); i++) {
				Period pObj = new Period();
				Node time = timeList.item(i);
				if (time.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) time;

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Date fromDate = dateFormat.parse(e.getAttribute("from"));
					Date toDate = dateFormat.parse(e.getAttribute("to"));
					
					pObj.setFromDate(fromDate);
					pObj.setToDate(toDate);

					NodeList timeChildList = e.getChildNodes();
					for (int j = 0; j < timeChildList.getLength(); j++) {
						Node ch = timeChildList.item(j);
						if (ch.getNodeType() == Node.ELEMENT_NODE) {
							Element ej = (Element) ch;
							String nodeName = ej.getNodeName();

							switch (nodeName) {

							case "symbol":
								pObj.setSkyStatus(ej.getAttribute("name"));
								break;
							case "windSpeed":
								pObj.setWindSpeed(ej.getAttribute("name"));
								break;
							case "temperature":
								pObj.setTemperature(Double.parseDouble(ej.getAttribute("value")));
								break;
							case "pressure":
								pObj.setPressure(Double.parseDouble(ej.getAttribute("value")));
								break;
							case "humidity":
								pObj.setHumidity(Double.parseDouble(ej.getAttribute("value")));
								break;

							}

						}
					}
				}
				wObj.addToPeriodList(pObj);;
			}
			}else {
				return null;
			}

		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return wObj;

	}

	public String getForecast(String city,String language,String standard,int rows) throws Exception {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		String uri = "http://api.openweathermap.org/data/2.5/forecast?q=" + city +"&mode=xml&lang="+language+"&units="+standard+"&cnt="+rows+"&APPID=7e7b1f6023f3d42eab030a4817826c8a";
		HttpGet getForecast = new HttpGet(uri);
		CloseableHttpResponse httpResponse = httpClient.execute(getForecast);
		HttpEntity httpEntity = httpResponse.getEntity();
		String xml = EntityUtils.toString(httpEntity);
		return xml;

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
