import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.util.*;
import java.awt.Desktop;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class is used to communicate with the test server.
 *
 * Errors should be reported via the webboard.
 * Critical errors may also be reported by mail to nis@cs.au.dk
 * Describe the problem as detailed and precise as possible.
 * If we cannot reproduce it, we cannot fix it.
 *
 * @author  Nikolaj I. Schwartzbach & Asger Phillip Andersen
 * @version 2021-09-20
 */
public class TestServer {
    private TestServer() {}

    /**
     * Downloads any available updates to the testserver file.
     * @throws IOException
     */
    public static void updateTestServer() {
        if(updateAvailable()) {
            try (ReadableByteChannel rbc = Channels.newChannel(new URL("https://adamkjelstroem.github.io/IntProg-undervisningsmateriale/web/e21/opgaver/TestServer.java").openStream());
                 FileOutputStream fos = new FileOutputStream("TestServer.java")) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                System.out.println("TestServeren er blevet opdateret :)");
            } catch (FileNotFoundException e) {
                System.out.println("Kan ikke overskrive den lokale fil.\nKontakt en adm. instruktor tak :)");
            } catch(MalformedURLException e) {
                System.out.println("Forkert URL...\nKontakt en adm. instruktor tak :)");
            } catch (IOException e) {
                System.out.println("FileOutputStream fejlede...\nKontakt en adm. instruktor tak :)");
            }
        } else {
            System.out.println("Du har allerede den nyeste version af TestServeren");
        }
    }

    /**
     * Checks if there is an update available, for the testserver.
     */
    private static void checkForUpdates() {
        boolean updateAvailable = updateAvailable();
        if(updateAvailable) {
            System.out.println("Der er en ny version af TestServeren. Kald updateTestServer metoden, for at opdatere");
        }
        else {
            System.out.println("Du har den nyeste version af TestServeren :)");
        }
    }

    /**
     * Checks if there is an update available, used in both the public method, and the download method
     * @return true when there is an update available, otherwise false.
     */
    private static boolean updateAvailable() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");

        try(InputStream local = new FileInputStream(new File("TestServer.java"));
            InputStream online = (new URL("https://adamkjelstroem.github.io/IntProg-undervisningsmateriale/web/e21/opgaver/TestServer.java")).openStream()) {
            LocalDate localDate = LocalDate.parse(parseVersionDate(local), dtf);
            LocalDate onlineDate = LocalDate.parse(parseVersionDate(online), dtf);

            return localDate.isBefore(onlineDate);
        } catch(MalformedURLException e) {
            System.out.println("Forkert URL...\nKontakt en adm. instruktor tak :)");
        } catch(FileNotFoundException e) {
            System.out.println("Kan finde lokale fil.\nKontakt en adm. instruktor tak :)");
        } catch(IOException e) {
            System.out.println("Kan ikke læse den lokale fil.\nKontakt en adm. instruktor tak :)");
        }

        return false;
    }

    /**
     * Parses the version date of the testserver file specified in the inputstream
     * @param testServerInputStream - An input stream to the testserver file
     * @return The version date of the given file, as a string. uuuu-MM-dd formatted.
     */
    private static String parseVersionDate(InputStream testServerInputStream) {
        Scanner s = new Scanner(testServerInputStream);
        String date = "";
        while(s.hasNextLine()) {
            String line = s.nextLine();
            if(line.length() > 11 && line.substring(3, 11).equals("@version")) {
                date = line.substring(12, 22);
                break;
            }
        }

        s.close();

        return date;
    }

    /**
     * Downloads the latest files from the course webpage.
     * @param exercise  one of: CG1, CG3, CG5
     */
    public static void download(String exercise) throws IOException {
        if(updateAvailable()) {
            updateTestServer();
            System.out.println("Du havde ikke den nyeste version af testserveren, men den er blevet opdateret nu.\nPrøv igen, og genstart eventuelt BlueJ.");
            return;
        }

        String ex = exercise.toLowerCase();

        switch(ex) {

            case "cg1":
                downloadCourseFiles("cg1","Game.java","GUI.java",
                        "Settings.java","Player.java","GUIPlayer.java","RandomPlayer.java",
                        "GreedyPlayer.java","SmartPlayer.java","CGTest.java",
                        "network.dat","map.png","greedyplayer.png","randomplayer.png",
                        "smartplayer.png","guiplayer.png");
                break;
            case "cg3":
                downloadCourseFiles("cg3","Game.java","GUI.java",
                        "Player.java","RandomPlayer.java",
                        "GreedyPlayer.java","SmartPlayer.java");
                break;
            case "cg5":
                downloadCourseFiles("cg5","LogPlayer.java","Game.java","Settings.java");
                break;
        }
    }

    /**
     * Tests exercise and open the resulting link in the default browser (if uploaded successfully).
     * @param exercise   Short name of exercise.
     */
    public static void testAndOpenInBrowser(String exercise) throws IOException, URISyntaxException {
        if(updateAvailable()) {
            updateTestServer();
			System.out.println("Du havde ikke den nyeste version af testserveren, men den er blevet opdateret nu.\nPrøv igen, og genstart eventuelt BlueJ.");            return;
        }

        String str = test(exercise);
        if(str.contains("Link: ")){
            str = str.substring(str.indexOf("Link: ")+6);
            Desktop.getDesktop().browse(new URL(str).toURI());
        }
    }

    /**
     * Tests exercise in the classpath (the folder from which the program is invoked).
     * @param exercise   Short name of exercise.
     */
    public static String test(String exercise) throws IOException {
        if(updateAvailable()) {
            updateTestServer();
			System.out.println("Du havde ikke den nyeste version af testserveren, men den er blevet opdateret nu.\nPrøv igen, og genstart eventuelt BlueJ.");            return "";
        }

        String ex = exercise.toLowerCase();
        // Files to be uploaded
        String[] files = null;
        switch(ex) {

            // DieCup
            case "dc1":
                files = new String[] {"Die","DieCup"}; break;
            case "dc2":
            case "dc3-1":
            case "dc3-4":
                files = new String[] {"Die","DieCup", "TestDriver"}; break;
            case "dc4-1":
            case "dc4-2":
                files = new String[] {"Die","DieCup", "DieTest", "DieCupTest"}; break;
            case "dc4-3":
                files = new String[] {"Game"}; break;

            // Turtle
            case "turtle1":
            case "turtle2":
                files = new String[] {"Actor", "Canvas", "Turtle"}; break;

            // 2016
            case "basketplayer":
                files = new String[] {"BasketPlayer","BasketTeam"}; break;
            case "cow":
                files = new String[] {"Cow","DiaryFarm"}; break;
            case "field-1":
            case "field-2":
                files = new String[] {"Field","Farm"}; break;
            case "goat":
                files = new String[] {"Goat","GoatFarm"}; break;
            case "handballplayer":
                files = new String[] {"HandballPlayer","HandballTeam"}; break;
            case "image":
                files = new String[] {"Image","UsbStick"}; break;
            case "soccerplayer":
                files = new String[] {"SoccerPlayer","SoccerTeam"}; break;
            case "volleyplayer":
                files = new String[] {"VolleyPlayer","VolleyTeam"}; break;
            case "passenger":
                files = new String[] {"Passenger","Ferry"}; break;
            case "picture":
                files = new String[] {"Picture","UsbStick"}; break;
            case "song":
                files = new String[] {"Song","DVD"}; break;

            // 2017
            case "animal":
                files = new String[] {"Animal","Zoo"}; break;
            case "brick-1":
            case "brick-2":
                files = new String[] {"Brick","LegoBox"}; break;
            case "carpet-1":
            case "carpet-2":
                files = new String[] {"Carpet","Shop"}; break;
            case "tool-1":
            case "tool-2":
                files = new String[] {"Tool","ToolBox"}; break;
            case "vegetable-1":
            case "vegetable-2":
                files = new String[] {"Vegetable","Shop"}; break;
            case "nail":
                files = new String[] {"Nail","Box"}; break;
            case "screw":
                files = new String[] {"Screw","Box"}; break;
            case "chicken":
                files = new String[] {"Chicken","ChickenYard"}; break;
            case "pigeon":
                files = new String[] {"Pigeon","PigeonLoft"}; break;
            case "penguin":
                files = new String[] {"Penguin","Group"}; break;

            // 2018
            case "ferry":
                files = new String[] {"Ferry","Harbour"}; break;
            case "train-1":
            case "train-2":
                files = new String[] {"Train","TrainStation"}; break;
            case "bus-1":
            case "bus-2":
                files = new String[] {"Bus","BusStation"}; break;
            case "flight":
                files = new String[] {"Flight","Airport"}; break;

            // 2019
            case "chapter":
                files = new String[]{ "Chapter", "Book" }; break;
            case "cheese":
                files = new String[]{ "Cheese", "Cooler" }; break;
            case "drink":
                files = new String[]{ "Drink", "Refrigerator" }; break;
            case "flower":
                files = new String[]{ "Flower", "Bouquet" }; break;
            case "food":
                files = new String[]{ "Food", "DeepFreezer" }; break;
            case "fruit":
                files = new String[]{ "Fruit", "Basket" }; break;
            case "pearl":
                files = new String[]{ "Pearl", "Necklace" }; break;
            case "photo":
                files = new String[]{ "Photo", "Album" }; break;

            // 2020
            case "cat":
                files = new String[]{ "Cat", "Family" }; break;
            case "dog":
                files = new String[]{ "Dog", "Kennel" }; break;
            case "elephant":
                files = new String[]{ "Elephant", "Zoo" }; break;
            case "fox":
                files = new String[]{ "Fox", "Island" }; break;
            case "horse":
                files = new String[]{ "Horse", "Farm" }; break;
            case "lion":
                files = new String[]{ "Lion", "Savannah" }; break;
            case "pet":
                files = new String[]{ "Pet", "Child" }; break;
            case "rabbit":
                files = new String[]{ "Rabbit", "Boy" }; break;
            case "mouse":
                files = new String[]{ "Mouse", "Girl" }; break;
            case "pig":
                files = new String[]{ "Pig", "Island" }; break;
            case "sheep":
                files = new String[]{ "Sheep", "Farm" }; break;
            case "tiger":
                files = new String[]{ "Tiger", "Savannah" }; break;

            // Videos
            case "phone":
                files = new String[] {"Phone","WebShop"}; break;
            case "pirate":
                files = new String[] {"Pirate","PirateShip"}; break;
            case "car":
                files = new String[] {"Car","Store"}; break;
            case "turtle":
                files = new String[] {"Turtle","Zoo"}; break;

            // Handins
            case "musician":
                files = new String[] {"Musician", "Band"}; break;
            case "racer":
                files = new String[] {"Racer", "FormulaOne"}; break;
            case "boat":
                files = new String[] {"Boat", "Marina"}; break;
            case "biker":
                files = new String[] {"Biker", "MotorcycleClub"}; break;
            case "film":
                files = new String[] {"Film", "FilmCollection"}; break;

            case "cg1-1":
                files = new String[] {"City"}; break;
            case "cg1-2":
                files = new String[] {"Road"}; break;
            case "cg1-3":
                files = new String[] {"Position"}; break;
            case "cg1-4":
                files = new String[] {"Country"}; break;
            case "cg1-5":
                files = new String[] {"City","Country"}; break;
            case "cg1-6":
                files = new String[] {"Country"}; break;
            case "cg1-7":
                files = new String[] {"Country"}; break;
            case "cg1":
                files = new String[] {"City","Road","Position","Country"}; break;

            case "cg2-1":
                files = new String[] {"RoadTest"}; break;
            case "cg2-2":
                files = new String[] {"PositionTest"}; break;
            case "cg2-3":
                files = new String[] {"CityTest"}; break;
            case "cg2-4":
                files = new String[] {"CountryTest"}; break;
            case "cg2":
                files = new String[] {"RoadTest","PositionTest","CityTest","CountryTest"}; break;

            case "cg3-1":
                files = new String[] {"BorderCity","BorderCityTest"}; break;
            case "cg3-2":
                files = new String[] {"CapitalCity","CapitalCityTest"}; break;
            case "cg3-3":
                files = new String[] {"MafiaCountry","MafiaCountryTest"}; break;
            case "cg3-4":
                files = new String[] {"City","CityTest"}; break;
            case "cg3":
                files = new String[] {"City","BorderCity","BorderCityTest","CapitalCity","CapitalCityTest","MafiaCountry","MafiaCountryTest"}; break;

            case "cg5":
                files = new String[] {"Log", "Settings"}; break;

            default:
                System.err.println("Invalid exercise: "+ exercise +"."); return "Invalid exercise: "+ exercise +".";
        }

        Map<String,String> arguments = new HashMap<>();

        // Check upload code
        String auID="", code = "";

        // Check if auID/code are saved
        Path dataPath = Paths.get("upload-data.dat");
        if(Files.exists(dataPath)){
            // Read first line
            String line = Files.readAllLines(dataPath).get(0);

            // Split into auID and code
            String[] data = line.split(" ");

            auID = data[0];
            code = data[1];
        }

        // Check if information is missing
        if(auID.isEmpty() || code.isEmpty()){
            // Get user input
            Scanner s = new Scanner(System.in);

            System.out.print("Første gang du uploader et projekt til testserveren skal du indtaste dit auID og en adgangskode.\n" +
                    "Begge dele huskes i din projektmappe, således at du ikke behøver at indtaste dem ved senere uploads af projektet.\n\n" +
                    "Du kan skaffe dig en adgangskode via linket: https://dintprog.cs.au.dk/reset.php\n\n" +
                    "Det er samme adgangskode, der anvendes til quizserveren og til testserveren.\n\n" +
                    "Indtast auID: \n> ");

            auID = s.nextLine();
            System.out.print("\nIndtast adgangskode: \n> ");
            code = s.nextLine();

            // Save information for next time
            PrintWriter pw = new PrintWriter("upload-data.dat");
            pw.println(auID + " " + code);
            pw.close();
        }

        // Set meta information
        arguments.put("h",ex.replace("dc3-1","dc3a").replace("dc3-4","dc3b").replace("dc4-1","dc4a").replace("dc4-2","dc4b").replace("dc4-2","dc4c"));
        arguments.put("auID",auID);
        arguments.put("code",code);

        // Check all files and accumulate contents
        for(String file : files) {
            Path p = Paths.get(file+".java");
            // Check if file exists
            if(Files.exists(p)) {
                String str = new String(Files.readAllBytes(p), Charset.defaultCharset());
                arguments.put(file.toLowerCase(), str);
            }
            else {
                System.err.println("No file with this name '" + file + ".java' in folder " + System.getProperty("user.dir")  +".");
                return "No file with this name '" + file + ".java' in folder " + System.getProperty("user.dir")  +".";
            }
        }

        // Send request and handle response
        String response = sendPost("https://dintprog.cs.au.dk/upload.php",arguments);
        response = response.replace("-server","");
        System.out.println(response);
        if(response.startsWith("Invalid")) {
            try{
                Files.delete(dataPath);
                test(ex);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * This method downloads a variable number of files as specified by files.
     * @param version   The version of the files to download. One of:  cg1, cg2, cg3.
     * @param files     The files to download.
     */
    private static void downloadCourseFiles(String version, String... files)  {
        for(String file : files)
            try {
                downloadFile("https://adamkjelstroem.github.io/IntProg-undervisningsmateriale/web/e21/opgaver/"+version+"/"+file,file);
            } catch(IOException e){
                System.err.println("Unable to download file: "+file+". Reason: "+e.getMessage());
            }

        JOptionPane.showMessageDialog(null, "Download af filer for version '"+version+"' blev gennemført succesfuldt. Det kan være nødvendigt at genstarte BlueJ for at se de nye filer.", "Filer downloadet succesfuldt.", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method downloads a file from a url and saves it to the local machine.
     * @param url The url from which to download.
     * @param dest The destination of the downloaded file.
     */
    private static void downloadFile(String url, String dest) throws IOException {
        if(Files.exists(Paths.get(dest)))
            Files.delete(Paths.get(dest));
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(dest);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    /**
     * This method sends an HTTP POST request to a specified location with some arguments.
     * @param location     URL to POST to.
     * @param arguments    HTTP header arguments.
     * @return   Response from the URL (if any).
     * @throws   IOException   For various reasons.
     */
    private static String sendPost(String location, Map<String,String> arguments) throws IOException {

        URL url = new URL(location);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        con.setRequestProperty("User-Agent", "Java client");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(entry.getValue(), "UTF-8"));
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        StringBuilder sb;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
            String line;
            sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        http.disconnect();
        return sb.toString();
    }
}
