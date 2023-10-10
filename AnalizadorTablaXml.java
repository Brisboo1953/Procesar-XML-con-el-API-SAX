import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AnalizadorTablaXml extends DefaultHandler {
    private static final String NOMBRE_CLASE = AnalizadorTablaXml.class.getSimpleName();
    private static final Logger LOG = Logger.getLogger(NOMBRE_CLASE);
    private static final List<String> nombresColumnas = new ArrayList<>();
    private SAXParserFactory fabricaSaxParser = SAXParserFactory.newInstance();
    private SAXParser saxParser = null;
    private String elementoActual;
    private String valorActual;
    private static final ArrayList<String> datosCelda = new ArrayList<>();
    private static final ArrayList<String> historialArchivos = new ArrayList<>();

    private JFrame marcoSelectorArchivo; // Marco principal para la selección de archivos

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AnalizadorTablaXml analizador = new AnalizadorTablaXml();
                analizador.MostrarSelArchivo();
            }
        });
    }

    private void MostrarSelArchivo() {
        marcoSelectorArchivo = new JFrame("Seleccionar Archivo XML");
        marcoSelectorArchivo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marcoSelectorArchivo.setSize(400, 200);
        marcoSelectorArchivo.setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JPanel panel = new JPanel(new GridBagLayout());
        marcoSelectorArchivo.add(panel);

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(10, 10, 10, 10);

        JLabel etiquetaTitulo = new JLabel("Hecho por Brisseida y Uziel");
        etiquetaTitulo.setForeground(Color.MAGENTA); // Texto en magenta
        restricciones.gridx = 0;
        restricciones.gridy = 0;
        restricciones.gridwidth = 2;
        panel.add(etiquetaTitulo, restricciones);

        JButton botonSeleccionar = new JButton("Seleccionar archivo XML");
        botonSeleccionar.setPreferredSize(new Dimension(200, 40));
        restricciones.gridx = 0;
        restricciones.gridy = 1;
        restricciones.gridwidth = 1;
        panel.add(botonSeleccionar, restricciones);

        JButton botonGuardarHistorial = new JButton("Guardar Historial");
        botonGuardarHistorial.setPreferredSize(new Dimension(200, 40));
        restricciones.gridx = 1;
        restricciones.gridy = 1;
        panel.add(botonGuardarHistorial, restricciones);

        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.setPreferredSize(new Dimension(200, 40));
        restricciones.gridx = 0;
        restricciones.gridy = 2;
        restricciones.gridwidth = 2;
        panel.add(botonCerrar, restricciones);

        botonSeleccionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser selectorArchivo = new JFileChooser();
                int resultado = selectorArchivo.showOpenDialog(null);

                if (resultado == JFileChooser.APPROVE_OPTION) {
                    File archivoSeleccionado = selectorArchivo.getSelectedFile();
                    historialArchivos.add(archivoSeleccionado.getAbsolutePath());

                    analizarArchivoXml(archivoSeleccionado);

                    String[] columnas = nombresColumnas.toArray(new String[0]);
                    int cantidadFilas = datosCelda.size() / nombresColumnas.size();
                    int indiceDatos = 0;
                    Object[][] datos = new Object[cantidadFilas][nombresColumnas.size()];

                    for (int indiceFila = 0; indiceFila < cantidadFilas; indiceFila++) {
                        for (int indiceColumna = 0; indiceColumna < nombresColumnas.size(); indiceColumna++) {
                            if (indiceDatos < datosCelda.size()) {
                                datos[indiceFila][indiceColumna] = datosCelda.get(indiceDatos);
                                indiceDatos++;
                            } else {
                                break;
                            }
                        }
                    }

                    mostrarTabla(columnas, datos);
                }
            }
        });

        botonGuardarHistorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarHistorial();
            }
        });

        botonCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Arrastrar y mover la ventana
        marcoSelectorArchivo.addMouseListener(new MouseAdapter() {
            private int posX, posY;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                posX = e.getX();
                posY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent evt) {
                int x = evt.getXOnScreen();
                int y = evt.getYOnScreen();
                marcoSelectorArchivo.setLocation(x - posX, y - posY);
            }
        });

        marcoSelectorArchivo.setVisible(true);
    }

    private void analizarArchivoXml(File archivoXml) {
        try {
            saxParser = fabricaSaxParser.newSAXParser();
        } catch (SAXException | ParserConfigurationException e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        }
        try {
            saxParser.parse(archivoXml, this);
        } catch (IOException | SAXException e) {
            LOG.severe(e.getMessage());
        }
    }

    private void mostrarTabla(String[] columnas, Object[][] datos) {
        JFrame marcoTabla = new JFrame("Tabla de Datos");
        marcoTabla.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        marcoTabla.setSize(600, 400);
        marcoTabla.setResizable(true);

        JTable tabla = new JTable(datos, columnas);
        tabla.setEnabled(false); // Hace que la tabla sea de solo lectura
        JScrollPane panelDesplazamiento = new JScrollPane(tabla);
        marcoTabla.add(panelDesplazamiento);
        marcoTabla.setLocationRelativeTo(marcoSelectorArchivo); // Abre la ventana cerca del selector de archivos
        marcoTabla.setVisible(true);
    }

    private void guardarHistorial() {
        try {
            FileWriter escritor = new FileWriter("historial_archivos.txt");
            for (String archivo : historialArchivos) {
                escritor.write(archivo + System.lineSeparator());
            }
            escritor.close();
            JOptionPane.showMessageDialog(null, "Historial de archivos guardado en historial_archivos.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar el historial de archivos");
        }
    }

    @Override
    public void startElement(String uri, String nombreLocal, String nombreQName, Attributes atributos) {
        elementoActual = nombreQName;
        valorActual = nombreLocal;
    }

    @Override
    public void endElement(String uri, String nombreLocal, String nombreQName) {
        elementoActual = null;
        valorActual = null;
    }

    @Override
    public void characters(char[] ch, int inicio, int longitud) {
        if (elementoActual != null && valorActual != null) {
            String valor = new String(ch, inicio, longitud);
            if (!valor.isEmpty()) {
                datosCelda.add(valor);

                String nombreColumna = elementoActual.trim();
                if (!nombreColumna.isEmpty() && !nombresColumnas.contains(nombreColumna)) {
                    nombresColumnas.add(nombreColumna);
                }
            }
        }
    }
}




