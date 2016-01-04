import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridLayout;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class Monitoring extends AppCompatActivity
{
    GraphicalView chart1, chart2;
    XYMultipleSeriesRenderer renderer;
    XYMultipleSeriesDataset series;
    XYSeriesRenderer sineRenderer, cosineRenderer, tangentRenderer;
    XYSeries sineSeries, cosineSeries, tangentSeries;
    Thread tt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

                // Get a reference of the layout
                GridLayout layout=(GridLayout)findViewById(R.id.waveforms);

                if(chart1==null)
                {
                    // Initialize the chart
                    initChart();

                    // Add data to the chart
                    tt = new Thread() {
                        public void run() {
                            // Add data to the chart series
                            for(int angle=5;angle<=1440;angle+=5)
                            {
                                try {
                                    Thread.sleep(5);
                                }
                                catch(Exception e)
                                {}

                                sineSeries.add(angle, Math.sin(angle * (Math.PI / 180)));
                                cosineSeries.add(angle, Math.cos(angle * (Math.PI / 180)));
                                /*tangentSeries.add(angle, Math.tan(angle*(Math.PI/180)));*/
                            }
                        }
                    };
                    tt.start();

                    // Create a line chart
                    chart1 = ChartFactory.getLineChartView(this, series, renderer);

                    // Add chart to the layout
                    layout.addView(chart1);
                }
                else
                {
                    // Refresh the chart
                    chart1.repaint();
                }
    }

    protected void initChart()
    {
        // Initialize renderers
        sineRenderer=new XYSeriesRenderer();
        cosineRenderer=new XYSeriesRenderer();
        tangentRenderer=new XYSeriesRenderer();

        // Set color for each series
        sineRenderer.setColor(Color.RED);
        cosineRenderer.setColor(Color.GREEN);
        tangentRenderer.setColor(Color.BLUE);

        // Create XYMultipleSeriesRenderer
        renderer=new XYMultipleSeriesRenderer();

        // Add renderers to XYMultipleSeriesRenderer
        renderer.addSeriesRenderer(sineRenderer);
        renderer.addSeriesRenderer(cosineRenderer);
        renderer.addSeriesRenderer(tangentRenderer);

        // Disable panning
        renderer.setPanEnabled(false);

        // Set Y-Axis range
        renderer.setYAxisMax(1);
        renderer.setYAxisMin(-1);

        // Initialize series
        sineSeries=new XYSeries("Sine");
        cosineSeries=new XYSeries("Cosine");
        tangentSeries=new XYSeries("Tangent");

        // Create XYMultipleSeriesDataset
        series=new XYMultipleSeriesDataset();

        // Add series to XYMultipleSeriesDataset
        series.addSeries(sineSeries);
        series.addSeries(cosineSeries);
        series.addSeries(tangentSeries);
    }

    protected void plotData()
    {
        // Add data to the chart series
        for(int angle=5;angle<=1440;angle+=5)
        {
            sineSeries.add(angle, Math.sin(angle * (Math.PI / 180)));
            cosineSeries.add(angle, Math.cos(angle * (Math.PI / 180)));
            /*tangentSeries.add(angle, Math.tan(angle*(Math.PI/180)));*/
        }
    }
}
