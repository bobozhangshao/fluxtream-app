package com.fluxtream.widgets.dataproviders.week;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.fluxtream.TimeInterval;
import com.fluxtream.domain.GuestSettings;
import com.fluxtream.widgets.dataproviders.AbstractWidgetDataProvider;

@Component("week/caloriesBurned")
public class CaloriesBurnedDataProvider extends AbstractWidgetDataProvider {

	@Override
	public JSONObject provideData(long guestId, GuestSettings settings,
			TimeInterval timeInterval) {
		JSONObject o = new JSONObject();
		return o;
	}

}