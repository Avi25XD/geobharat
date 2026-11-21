package __APP_ID__;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.util.Calendar;

/** Home-screen widget: shows a new India geography fact every day. Tap to open the quiz, tap the arrow for the next fact. */
public class FactWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_NEXT = "__APP_ID__.WIDGET_NEXT";
    private static final String PREFS = "geobharat_widget";

    private static final String[] FACTS = {
        "Kangchenjunga (8,586 m) in Sikkim is the highest peak in India.",
        "The Ganga, about 2,525 km long, is the longest river of India.",
        "Rajasthan is the largest Indian state by area, and Goa is the smallest.",
        "Mawsynram in Meghalaya is one of the wettest places on Earth.",
        "Chilika in Odisha is India's largest coastal lagoon.",
        "Indira Point in the Nicobar Islands is the southernmost point of India.",
        "The Tropic of Cancer passes through eight Indian states.",
        "The Sundarbans is the world's largest mangrove forest and home to the Royal Bengal tiger.",
        "Wular Lake in Jammu and Kashmir is the largest freshwater lake in India.",
        "Gir in Gujarat is the only home of the wild Asiatic lion.",
        "Kaziranga in Assam shelters most of the world's one-horned rhinoceroses.",
        "Sambhar in Rajasthan is the largest inland salt lake in India.",
        "Vembanad in Kerala is the longest lake in India.",
        "The Narmada and the Tapi flow west into the Arabian Sea through rift valleys.",
        "The Brahmaputra is called the Tsangpo in Tibet.",
        "Uttar Pradesh is the most populous state of India.",
        "The Western Ghats are a UNESCO World Heritage Site and a biodiversity hotspot.",
        "The Palk Strait separates India from Sri Lanka.",
        "Zoji La links Srinagar with Ladakh.",
        "Nathu La, a pass in Sikkim, connects India with Tibet.",
        "Lakshadweep is the smallest union territory of India by area.",
        "India has 28 states and 8 union territories.",
        "The Godavari is the longest river of peninsular India.",
        "Keibul Lamjao in Manipur is the only floating national park in the world.",
        "Dal Lake, famous for its houseboats, is in Srinagar.",
        "The Himalayas are young fold mountains and are still rising.",
        "The Aravalli range is among the oldest fold mountain ranges in the world.",
        "The Ten Degree Channel separates the Andaman Islands from the Nicobar Islands.",
        "The Siliguri Corridor, nicknamed the Chicken's Neck, links north-east India with the rest of the country.",
        "Gujarat has the longest coastline among Indian states.",
        "Anamudi (2,695 m) in Kerala is the highest peak in peninsular India.",
        "Regur, the black soil of the Deccan, is ideal for growing cotton.",
        "Indian Standard Time is based on the 82.5 degrees East longitude, near Mirzapur.",
        "The Thar Desert lies mostly in Rajasthan.",
        "Loktak Lake in Manipur is known for its floating islands of vegetation called phumdis."
    };

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) update(ctx, mgr, id);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context ctx, AppWidgetManager mgr, int id, android.os.Bundle opts) {
        update(ctx, mgr, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (ACTION_NEXT.equals(intent.getAction())) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            p.edit().putInt("skip", p.getInt("skip", 0) + 1).apply();
            AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, FactWidgetProvider.class));
            for (int id : ids) update(ctx, mgr, id);
        }
    }

    private static void update(Context ctx, AppWidgetManager mgr, int id) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Calendar c = Calendar.getInstance();
        String today = c.get(Calendar.YEAR) + "-" + c.get(Calendar.DAY_OF_YEAR);
        if (!today.equals(p.getString("day", ""))) {
            p.edit().putString("day", today).putInt("skip", 0).apply();
        }
        int index = Math.floorMod(c.get(Calendar.YEAR) * 366 + c.get(Calendar.DAY_OF_YEAR) + p.getInt("skip", 0), FACTS.length);

        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_fact);
        v.setTextViewText(R.id.fact_text, FACTS[index]);

        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent openPi = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.widget_root, openPi);

        Intent next = new Intent(ctx, FactWidgetProvider.class).setAction(ACTION_NEXT);
        PendingIntent nextPi = PendingIntent.getBroadcast(ctx, 1, next, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.fact_next, nextPi);

        mgr.updateAppWidget(id, v);
    }
}
