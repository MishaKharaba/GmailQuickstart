package com.example.misha.gmailquickstart;

import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;

public class ExchangeReceiver
{

    public List<String> getFolders() throws Exception
    {
        ExchangeService service = new ExchangeService();
        ExchangeCredentials credentials = new WebCredentials("misha_kharaba@hotmail.com", "");

        service.setCredentials(credentials);
        service.autodiscoverUrl("misha_kharaba@hotmail.com");

        return null;
    }
}
