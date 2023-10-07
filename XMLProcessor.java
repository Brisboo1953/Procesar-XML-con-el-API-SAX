import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.text.DecimalFormat; 
import java.util.ArrayList;

public class XMLProcessor {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException { 
        try {
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

            SAXParser saxParser = saxParserFactory.newSAXParser();

            CDHandler handler = new CDHandler();

            saxParser.parse("cd_catalog.xml", handler);

            ArrayList<Double> prices = handler.getCdPrices();
            double sum = 0;
            for (double price : prices) {
                sum = sum + price;
            }
            double mean = sum / prices.size();

            double differenceBtwnPrices = 0;
            for (double price : prices) {
                differenceBtwnPrices = differenceBtwnPrices + Math.pow(price - mean, 2);
            }
            double deviation = Math.sqrt(differenceBtwnPrices / prices.size());

            System.out.print("Precios de los CDs: ");
            for (int i = 0; i < prices.size(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(formatPrice(prices.get(i)));
            }
            System.out.println(); 
            System.out.println("Valor de la suma total de los precios: " + formatPrice(sum)); 
            System.out.println("______________________________________");
            System.out.println("\nMedia de los precios: " + formatPrice(mean)); 
            System.out.println("Desviación estándar de los precios: " + formatPrice(deviation)); 

        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Se ha generado una excepcion" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("$0.00"); 
        return df.format(price);
    }
}

class CDHandler extends DefaultHandler {

    private ArrayList<Double> pricesList = new ArrayList<>();
    private boolean captureInitializer = false;

    public ArrayList<Double> getCdPrices() {
        return pricesList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("PRICE")) {
            captureInitializer = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (captureInitializer) {
            String priceStr = new String(ch, start, length);
            double price = Double.parseDouble(priceStr);
            pricesList.add(price);
            captureInitializer = false;
        }
    }
}

