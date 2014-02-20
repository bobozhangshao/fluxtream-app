package com.fluxtream.connectors.updaters;

import com.fluxtream.domain.SharedConnector;

/**
 * User: candide
 * Date: 27/07/13
 * Time: 13:17
 */
public interface SharedConnectorSettingsAwareUpdater {

    /**
     * This is called after a successful connector update (historical or incremental) and
     * lets the updater update the provided settings with the freshest data.
     * @param updateInfo
     * @param sharedConnector
     */
    void syncSharedConnectorSettings(final UpdateInfo updateInfo, final SharedConnector sharedConnector);

}
