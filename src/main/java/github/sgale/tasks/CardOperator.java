package github.sgale.tasks;

import com.google.gson.Gson;
import github.sgale.cardOperations.CardFinder;
import github.sgale.cardOperations.FieldUpdater;
import github.sgale.cardOperations.MediaSaver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;

import static github.sgale.tasks.PropertyGenerator.getSetting;

public class CardOperator {
    private final String input;
    private final String ANKI_URL;
    protected final Gson gson = new Gson();

    public CardOperator(String input) {
        this.input = input;
        this.ANKI_URL = getSetting("ankiUrl");
    }

    public void applyMediaToCards() {
        CardFinder cardFinder = new CardFinder();
        MediaSaver mediaSaver = new MediaSaver(input);

        try {
            long[] cardIds = cardFinder.findByTag();
            if(cardIds.length == 0) {
                throw new NoSuchElementException("Cards not found");
            }

            mediaSaver.store();

            for(long cardId: cardIds) {
                System.out.println(cardId);
                new FieldUpdater(input, cardId).changeField("media");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void translateCardsGlossary() {
        CardFinder cardFinder = new CardFinder();

        try {
            long[] cardIds = cardFinder.findByTag();
            for(long cardId: cardIds) {
                String glossary = new FieldUpdater("", cardId).getGlossaryValue();
                String translatedGlossary = new TextTranslator(glossary).translateGlossary();
                new FieldUpdater(translatedGlossary, cardId).changeField("glossary");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected HttpURLConnection createConnection(String method) throws IOException {
        URL url = new URL(ANKI_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        return conn;
    }

    protected String getInputName() {
        int indexOfSlash = input.lastIndexOf('\\');
        if (indexOfSlash < 0) {
            indexOfSlash = input.lastIndexOf('/');
        }

        return input.substring(indexOfSlash+1);
    }

    protected String getInput() {
        return input;
    }
}
