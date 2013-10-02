package com.fluxtream.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.fluxtream.Configuration;
import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.ApiKey;
import com.fluxtream.domain.ConnectorInfo;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.SystemService;
import com.fluxtream.utils.JPAUtils;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("singleton")
@Transactional(readOnly=true)
public class SystemServiceImpl implements SystemService, ApplicationListener<ContextRefreshedEvent> {

    static final Logger logger = Logger.getLogger(SystemServiceImpl.class);

    @Autowired
    ConnectorUpdateService connectorUpdateService;

	@Autowired
	Configuration env;

    @Autowired
    GuestService guestService;

	@PersistenceContext
	EntityManager em;

	static Map<String, Connector> scopedApis = new Hashtable<String, Connector>();

    static {
        scopedApis.put("https://www.googleapis.com/auth/latitude.all.best",
                       Connector.getConnector("google_latitude"));
        scopedApis.put("https://www.googleapis.com/auth/calendar.readonly",
                       Connector.getConnector("google_calendar"));
    }

    @Override
	public List<ConnectorInfo> getConnectors() throws Exception {
		List<ConnectorInfo> all = JPAUtils.find(em, ConnectorInfo.class, "connectors.all", (Object[])null);
		if (all.size() == 0) {
			resetConnectorList();
			all = JPAUtils.find(em, ConnectorInfo.class, "connectors.all",
					(Object[]) null);
		}
		for (ConnectorInfo connectorInfo : all) {
			connectorInfo.image = "/" + env.get("release")
					+ connectorInfo.image;
		}
		return all;
	}

    @Override
    public ConnectorInfo getConnectorInfo(final String connectorName) {
        List<ConnectorInfo> all = JPAUtils.find(em, ConnectorInfo.class, "connectors.all", (Object[])null);
        if (all.size() == 0) {
            initializeConnectorList();
        }
        final ConnectorInfo connectorInfo = JPAUtils.findUnique(em, ConnectorInfo.class, "connector.byName", connectorName);
        return connectorInfo;
    }

    @Transactional(readOnly = false)
    private void initializeConnectorList() {
		ResourceBundle res = ResourceBundle.getBundle("messages/connectors");
        int order = 0;

        final String facebook = "Facebook";
        String[] facebookKeys = checkKeysExist(facebook, Arrays.asList("facebook.appId", "facebook.appSecret"));
        final ConnectorInfo facebookConnectorInfo = new ConnectorInfo(facebook,
                                                                   "/images/connectors/connector-facebook.jpg",
                                                                   res.getString("facebook"),
                                                                   "/facebook/token",
                                                                   Connector.getConnector("facebook"), order++, facebookKeys!=null,
                                                                   false, false, facebookKeys);
        em.persist(facebookConnectorInfo);
        final String moves = "Moves";
        String[] movesKeys = checkKeysExist(moves, Arrays.asList("moves.client.id", "moves.client.secret", "moves.validRedirectURL", "foursquare.client.id", "foursquare.client.secret"));
        final ConnectorInfo movesConnectorInfo = new ConnectorInfo(moves,
                                                                   "/images/connectors/connector-moves.jpg",
                                                                   res.getString("moves"),
                                                                   "/moves/oauth2/token",
                                                                   Connector.getConnector("moves"), order++, movesKeys!=null,
                                                                   false, true, movesKeys);
        em.persist(movesConnectorInfo);
        final String latitude = "Google Latitude";
        String[] latitudeKeys = checkKeysExist(latitude, Arrays.asList("google.client.id", "google.client.secret"));
        final ConnectorInfo latitudeConnectorInfo = new ConnectorInfo(latitude,
                                                                      "/images/connectors/connector-google_latitude.jpg",
                                                                      res.getString("google_latitude"),
                                                                      "upload:google_latitude",
                                                                      Connector.getConnector("google_latitude"), order++, latitudeKeys!=null,
                                                                      true, false, latitudeKeys);
        latitudeConnectorInfo.supportsRenewTokens = false;
        latitudeConnectorInfo.renewTokensUrlTemplate = "google/oauth2/%s/token?scope=https://www.googleapis.com/auth/latitude.all.best";
        em.persist(latitudeConnectorInfo);
        final String fitbit = "Fitbit";
        String[] fitbitKeys = checkKeysExist(fitbit, Arrays.asList("fitbitConsumerKey", "fitbitConsumerSecret"));
        em.persist(new ConnectorInfo(fitbit,
                                     "/images/connectors/connector-fitbit.jpg",
                                     res.getString("fitbit"), "/fitbit/token",
                                     Connector.getConnector("fitbit"), order++, fitbitKeys!=null,
                                     false, true, fitbitKeys));
        final String bodyMedia = "BodyMedia";
        String[] bodymediaKeys = checkKeysExist(bodyMedia, Arrays.asList("bodymediaConsumerKey", "bodymediaConsumerSecret"));
        final ConnectorInfo bodymediaConnectorInfo = new ConnectorInfo(bodyMedia,
                                                                       "/images/connectors/connector-bodymedia.jpg",
                                                                       res.getString("bodymedia"),
                                                                       "/bodymedia/token",
                                                                       Connector.getConnector("bodymedia"), order++, bodymediaKeys!=null,
                                                                       false, true, bodymediaKeys);
        bodymediaConnectorInfo.supportsRenewTokens = true;
        bodymediaConnectorInfo.renewTokensUrlTemplate = "bodymedia/token?apiKeyId=%s";
        em.persist(bodymediaConnectorInfo);

        final String withings = "Withings";
        String[] withingsKeys = checkKeysExist(withings, Arrays.<String>asList());
        em.persist(new ConnectorInfo(withings,
                                     "/images/connectors/connector-withings.jpg",
                                     res.getString("withings"),
                                     "ajax:/withings/enterCredentials",
                                     Connector.getConnector("withings"), order++, withingsKeys!=null,
                                     false, true, withingsKeys));

        final String zeo = "Zeo";
        String[] zeoKeys = checkKeysExist(zeo, new ArrayList<String>());
        // Zeo no longer supports sync.  The myzeo servers were disabled due to bankruptcy in May/June 2013
        em.persist(new ConnectorInfo(zeo,
                                     "/images/connectors/connector-zeo.jpg",
                                     res.getString("zeo"),
                                     "ajax:/zeo/enterCredentials",
                                     Connector.getConnector("zeo"), order++, zeoKeys!=null,
                                     false, false, zeoKeys));
        final String mymee = "Mymee";
        em.persist(new ConnectorInfo(mymee,
                                     "/images/connectors/connector-mymee.jpg",
                                     res.getString("mymee"),
                                     "ajax:/mymee/enterFetchURL",
                                     Connector.getConnector("mymee"), order++, true,
                                     false, true, null));
        final String quantifiedMind = "QuantifiedMind";
        String[] quantifiedMindKeys = checkKeysExist(quantifiedMind, new ArrayList<String>());
        em.persist(new ConnectorInfo(quantifiedMind,
                                     "/images/connectors/connector-quantifiedmind.jpg",
                                     res.getString("quantifiedmind"),
                                     "ajax:/quantifiedmind/getTokenDialog",
                                     Connector.getConnector("quantifiedmind"), order++, quantifiedMindKeys!=null,
                                     false, true, quantifiedMindKeys));
        final String flickr = "Flickr";
        String[] flickrKeys = checkKeysExist(flickr, Arrays.asList("flickrConsumerKey", "flickrConsumerSecret", "flickr.validRedirectURL"));
        em.persist(new ConnectorInfo(flickr,
                                     "/images/connectors/connector-flickr.jpg",
                                     res.getString("flickr"),
                                     "/flickr/token",
                                     Connector.getConnector("flickr"), order++, flickrKeys!=null,
                                     false, true, flickrKeys));
        final String googleCalendar = "Google Calendar";
        String[] googleCalendarKeys = checkKeysExist(googleCalendar, Arrays.asList("google.client.id", "google.client.secret"));
        final ConnectorInfo googleCalendarConnectorInfo =
                new ConnectorInfo(googleCalendar,
                                  "/images/connectors/connector-google_calendar.jpg",
                                  res.getString("google_calendar"),
                                  "/google/oauth2/token?scope=https://www.googleapis.com/auth/calendar.readonly",
                                  Connector.getConnector("google_calendar"),
                                  order++, googleCalendarKeys != null, false, true, googleCalendarKeys);
        googleCalendarConnectorInfo.supportsRenewTokens = true;
        googleCalendarConnectorInfo.renewTokensUrlTemplate = "google/oauth2/%s/token?scope=https://www.googleapis.com/auth/calendar.readonly";
        em.persist(googleCalendarConnectorInfo);
        final String lastFm = "Last fm";
        String[] lastFmKeys = checkKeysExist(lastFm, Arrays.asList("lastfmConsumerKey", "lastfmConsumerSecret"));
        em.persist(new ConnectorInfo(lastFm,
                                     "/images/connectors/connector-lastfm.jpg",
                                     res.getString("lastfm"),
                                     "/lastfm/token",
                                     Connector.getConnector("lastfm"), order++, lastFmKeys!=null,
                                     false, true, lastFmKeys));
        final String twitter = "Twitter";
        String[] twitterKeys = checkKeysExist(twitter, Arrays.asList("twitterConsumerKey", "twitterConsumerSecret"));
        em.persist(new ConnectorInfo(twitter,
                                     "/images/connectors/connector-twitter.jpg",
                                     res.getString("twitter"), "/twitter/token",
                                     Connector.getConnector("twitter"), order++, twitterKeys!=null,
                                     false, true, twitterKeys));
        final String fluxtreamCapture = "Fluxtream Capture";
        String[] fluxtreamCaptureKeys = checkKeysExist(fluxtreamCapture, new ArrayList<String>());
        em.persist(new ConnectorInfo(fluxtreamCapture,
                                     "/images/connectors/connector-fluxtream_capture.png",
                                     res.getString("fluxtream_capture"),
                                     "ajax:/fluxtream_capture/about",
                                     Connector.getConnector("fluxtream_capture"), order++, fluxtreamCaptureKeys!=null,
                                     false, true, fluxtreamCaptureKeys));
        String[] runkeeperKeys = checkKeysExist("Runkeeper", Arrays.asList("runkeeperConsumerKey", "runkeeperConsumerSecret"));
        final String runKeeper = "RunKeeper";
        em.persist(new ConnectorInfo(runKeeper,
                                     "/images/connectors/connector-runkeeper.jpg",
                                     res.getString("runkeeper"),
                                     "/runkeeper/token",
                                     Connector.getConnector("runkeeper"), order++, runkeeperKeys!=null,
                                     false, true, runkeeperKeys));
        em.persist(new ConnectorInfo("SMS Backup",
                                     "/images/connectors/connector-sms_backup.jpg",
                                     res.getString("sms_backup"),
                                     "ajax:/smsBackup/enterCredentials",
                                     Connector.getConnector("sms_backup"), order++, true,
				     false,true,null));
	}

    private String[] checkKeysExist(String connectorName, List<String> keys) {
        String[] checkedKeys = new String[keys.size()];
        int i=0;
        boolean fatalMissingKey=false;
        boolean nonFatalMissingKey=false;

        for (String key : keys) {
            String value = env.get(key);
            if (value==null) {
                fatalMissingKey=true;
                String msg = "Couldn't find key \"" + key + "\" while initializing the connector table.  You need to add that key to your properties files.\n" +
                        "  See fluxtream-web/src/main/resources/samples/oauth.properties for details.";
                logger.info(msg);
                System.out.println(msg);
            } else if (value.equals("xxx")) {
                nonFatalMissingKey=true;
                String msg = "**** Found key \"" + key + "=xxx\" while populating the connector table.  Disabling the " + connectorName + " connector";
                logger.info(msg);
                System.out.println(msg);
            } else {
                checkedKeys[i++] = key;
            }
        }

        if(fatalMissingKey) {
            String msg = "***** Exiting execution due to missing configuration keys. See fluxtream-web/src/main/resources/samples/oauth.properties for details.";
            logger.info(msg);
            System.out.println(msg);
            System.exit(-1);
        }
        else if(nonFatalMissingKey) {
            return null;
        }
        return checkedKeys;
    }

    private String singlyAuthorizeUrl(final String service) {
        return (new StringBuilder("https://api.singly.com/oauth/authorize?client_id=")
            .append(env.get("singly.client.id"))
            .append("&redirect_uri=")
            .append(env.get("homeBaseUrl"))
            .append("singly/")
                    .append(service)
                    .append("/callback")
            .append("&service=")
            .append(service)).toString();
    }

    @Override
	public Connector getApiFromGoogleScope(String scope) {
		return scopedApis.get(scope);
	}

    @Transactional(readOnly = false)
    public void resetConnectorList() throws Exception {
        System.out.println("Resetting connector table");
        // Clear the existing data out of the Connector table
        JPAUtils.execute(em,"connector.deleteAll");
        // The following call will initialize the Connector table by calling
        // the initializeConnectorList function and return the result
        initializeConnectorList();
    }

    public boolean checkConnectorInstanceKeys(List<ConnectorInfo> connectors)
    {
        // For each connector type in connectorInfos which is enabled, make sure that all of the existing connector
        // instances have stored apiKeyAttributeKeys.  This is to support safe migration to version 0.9.0017.
        // Prior versions relied to continued coherence between the keys in the properties files
        // in fluxtream-web/src/main/resources and the existing connector instances.  However, that behavior
        // conflicted with migrating a given machine to a different host name or migrating a given DB to a
        // different server without breaking sync capability for existing connector instances.
        //
        // The new behavior stores the apiKeyAttributeKeys from the properties file in the ApiKeyAttribute
        // table for each connector instance, which makes it more portable but also incurrs a migration
        // requirement.  This function checks whether that migration needs to be performed for a given DB
        // instance
        JSONArray connectorsArray = new JSONArray();
        boolean missingKeys=false;

        for (int i = 0; i < connectors.size(); i++) {
            final ConnectorInfo connectorInfo = connectors.get(i);
            final Connector api = connectorInfo.getApi();
            if (api == null) {
                StringBuilder sb = new StringBuilder("module=SystemServiceImpl component=connectorStore action=checkConnectorInstanceKeys ")
                        .append("message=\"null connector for " + connectorInfo.getName() + "\"");
                logger.warn(sb.toString());
                continue;
            }
            if(connectorInfo.enabled==false) {
                StringBuilder sb = new StringBuilder("module=SystemServiceImpl component=connectorStore action=checkConnectorInstanceKeys ")
                        .append("message=\"skipping connector instance keys check for disabled connector" + connectorInfo.getName() + "\"");
                logger.info(sb.toString());
                continue;
            }
            String[] apiKeyAttributeKeys = connectorInfo.getApiKeyAttributesKeys();
            if(apiKeyAttributeKeys==null) {
                StringBuilder sb = new StringBuilder("module=SystemServiceImpl component=connectorStore action=checkConnectorInstanceKeys ")
                        .append("message=\"skipping connector instance keys check for connector" + connectorInfo.getName() + "; does not use keys\"");
                logger.info(sb.toString());
                continue;
            }
            // This connector type is enabled, find all the instance keys for this connector type
            List<ApiKey> apiKeys = JPAUtils.find(em, ApiKey.class, "apiKeys.all.byApi", api.value());
            for(ApiKey apiKey: apiKeys) {
                StringBuilder sb = new StringBuilder("module=SystemServiceImpl component=connectorStore action=checkConnectorInstanceKeys apiKeyId=" + apiKey.getId())
                                .append(" message=\"checking connector instance keys for connector" + connectorInfo.getName() + "\"");

                logger.info(sb.toString());

                // Iterate over the apiKeyAttributeKeys to check if each is present
                for(String apiKeyAttributeKey: apiKeyAttributeKeys) {
                    String apiKeyAttributeValue = guestService.getApiKeyAttribute(apiKey, apiKeyAttributeKey);
                    if(apiKeyAttributeValue==null) {
                        missingKeys=true;
                        String msg = "**** Missing key \"" + apiKeyAttributeKey + "\" for apiKeyId=" + apiKey.getId() + " api=" + api.value()
                                ;
                        StringBuilder sb2 = new StringBuilder("module=SystemServiceImpl component=connectorStore action=checkConnectorInstanceKeys apiKeyId=" + apiKey.getId())
			    .append(" message=\"").append(msg).append("\"");
                        logger.info(sb2.toString());
                        System.out.println(msg);
                    }
                }
            }

        }
        return missingKeys;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        System.out.println("ApplicationContext started");
        try {
            resetConnectorList();
            List<ConnectorInfo> connectors = getConnectors();
            boolean missingKeys=checkConnectorInstanceKeys(connectors);

            if(missingKeys) {
                String msg = "***** Exiting execution due to missing connector instance keys.\n  Check out fluxtream-admin-tools project, build, and execute 'java -jar target/flx-admin-tools.jar 5'";
                logger.info(msg);
                System.out.println(msg);
                System.exit(-1);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
