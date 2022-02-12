#include <ArduinoJson.h>
#include <RTCZero.h>
#include <ArduinoLowPower.h>
#include <WiFiNINA.h>
#include "Secrets.h"

const int incrPin = 14, decrPin = 15, editAlTmPin = 16, showTmPin = 19, stopAlPin = 17, speakerPin = 18, photoResPin = 21,
          hoursPins[] = {6, 5, 4, 3, 2}, minutesPins[] = {12, 11, 10, 9, 8, 7};

boolean idle, wakeupInterrupt, set = false;
const int dblClickDur = 500, showTimeDur = 10 * 1000, timeRefreshInterval = 500, updateInterval = 300 * 1000;
int32_t dblClickBeg, showTimeBeg, lastTimeRefresh, lastUpdate;

RTCZero rtc;
boolean alarmRinging, getTimePending, updateAlTmPending;
unsigned long alarmEpoch;
const int soundOn = 920, soundOff = 1000;
int32_t lastSound;

const char ssid[] = SECRET_SSID, pass[] = SECRET_PASS, gitToken[] = SECRET_GIT_TOKEN;
const char timeServerHost[] = "worldtimeapi.org", timeServerPath[] = "/api/ip";
const char alTmHost[] = "raw.githubusercontent.com", alTmPath[] = "/Debussy69/ArduinoAlarm/main/data/alarm_times.json", userAgent[] = "Debussy69";
int wifiStatus = WL_IDLE_STATUS;
String response;

void setup()
{
  idle = false;
  wakeupInterrupt = false;

  alarmRinging = false;
  getTimePending = true;
  updateAlTmPending = true;

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(speakerPin, OUTPUT);
  for (int pin : hoursPins)
  {
    pinMode(pin, OUTPUT);
  }
  for (int pin : minutesPins)
  {
    pinMode(pin, OUTPUT);
  }
  digitalWrite(speakerPin, LOW);

  pinMode(incrPin, INPUT_PULLUP);
  pinMode(decrPin, INPUT_PULLUP);
  pinMode(editAlTmPin, INPUT_PULLUP);
  pinMode(stopAlPin, INPUT_PULLUP);
  pinMode(showTmPin, INPUT_PULLUP);
  pinMode(photoResPin, INPUT_PULLUP);
  LowPower.attachInterruptWakeup(digitalPinToInterrupt(showTmPin), onShowTmInterrupt, RISING);
  LowPower.attachInterruptWakeup(digitalPinToInterrupt(photoResPin), onPhotoResInterrupt, CHANGE);

  Serial.begin(9600);
  delay(5000);
  Serial.println("************************************");
  digitalWrite(LED_BUILTIN, HIGH);

  onPhotoResInterrupt();

  rtc.begin();
}

void loop()
{
  int64_t ms = millis();

  if ((!idle || wakeupInterrupt) && ms - lastTimeRefresh > timeRefreshInterval && !getTimePending)
  {
    lastTimeRefresh = ms;
    showTime(rtc.getEpoch());
  }

  if (alarmRinging)
  {
    if (ms - lastSound > soundOff)
    {
      digitalWrite(speakerPin, LOW);
      lastSound = ms;
    }
    else if (ms - lastSound > soundOn)
    {
      digitalWrite(speakerPin, HIGH);
    }

    if (digitalRead(stopAlPin) == LOW)
    {
      alarmRinging = false;
      getTimePending = true;
      updateAlTmPending = true;
    }
  }
  else
  {
    if (getTimePending)
    {
      getTime();
    }
    else if (updateAlTmPending)
    {
      updateAlTm();
    }
    else if (idle)
    {
      if (wakeupInterrupt)
      {
        if (ms - showTimeBeg < showTimeDur)
          return;
        wakeupInterrupt = false;
      }

      int timeTilAlarm = (alarmEpoch - rtc.getEpoch()) * 1000;
      if (timeTilAlarm > updateInterval || alarmEpoch == 0)
      {
        hideTime();
        digitalWrite(LED_BUILTIN, LOW);
        LowPower.sleep(updateInterval);
        digitalWrite(LED_BUILTIN, HIGH);

        getTimePending = true;
        updateAlTmPending = true;
      }
      else if (timeTilAlarm > 0)
      {
        hideTime();
        digitalWrite(LED_BUILTIN, LOW);
        LowPower.sleep(timeTilAlarm);
        digitalWrite(LED_BUILTIN, HIGH);

        if (!wakeupInterrupt)
        {
          alarmRinging = true;
          showTime(rtc.getEpoch());
        }
      }
      else
      {
        alarmRinging = true;
        showTime(rtc.getEpoch());
      }
    }
    else
    {
      int timeTilAlarm = (alarmEpoch - rtc.getEpoch()) * 1000;
      if (ms - lastUpdate > updateInterval && (timeTilAlarm > updateInterval || alarmEpoch == 0))
      {
        getTimePending = true;
        updateAlTmPending = true;
        lastUpdate = ms;
      }
      else if (alarmEpoch != 0 && timeTilAlarm <= 0)
      {
        alarmRinging = true;
      }
    }
  }
}

void getTime()
{

  digitalWrite(LED_BUILTIN, LOW);
  connectToWiFi();

  Serial.println("#Getting Time");
  WiFiClient client;

  if (client.connect(timeServerHost, 80))
  {
    client.print("GET ");
    client.print(timeServerPath);
    client.println(" HTTP/1.1");
    client.print("Host: ");
    client.println(timeServerHost);
    client.println("Connection: close");
    client.println();

    response = client.readString();
  }
  else
  {
    Serial.println("#Connection failed");
    return;
  }
  client.flush();
  client.stop();

  response = response.substring(response.indexOf('{'));

  DynamicJsonDocument doc(2048);
  DeserializationError error = deserializeJson(doc, response);
  if (error)
  {
    Serial.print("#Deserialize Json failed: ");
    Serial.println(error.f_str());
    return;
  }

  rtc.setEpoch(doc["unixtime"].as<int32_t>() + doc["dst_offset"].as<int16_t>() + doc["raw_offset"].as<int16_t>());
  getTimePending = false;
  digitalWrite(LED_BUILTIN, HIGH);
  Serial.print("#Getting Time succesful: ");
  Serial.println(rtc.getEpoch());
  Serial.println("------------------------------");
}

void updateAlTm()
{
  digitalWrite(LED_BUILTIN, LOW);
  connectToWiFi();

  Serial.println("#Updating Alarm Time");

  WiFiSSLClient client;

  if (client.connect(alTmHost, 443))
  {
    client.print("GET ");
    client.print(alTmPath);
    client.println(" HTTP/1.1");
    client.print("Host: ");
    client.println(alTmHost);
    client.print("User-Agent: ");
    client.println(userAgent);
    client.println("Connection: close");
    client.println();

    response = client.readString();
  }
  else
  {
    Serial.println("#Connection failed");
    return;
  }
  client.flush();
  client.stop();

  response = response.substring(response.indexOf('{'));
  DynamicJsonDocument alTms(2048);
  DeserializationError error = deserializeJson(alTms, response);
  if (error)
  {
    Serial.print("#Deserialize Json failed: ");
    Serial.println(error.f_str());
    return;
  }

  int alTmsSize = alTms.size();
  if (alTmsSize < 7)
  {
    Serial.println("#Alarm Times invalid");
    return;
  }

  int epoch = rtc.getEpoch();
  int timeOfDay = epoch % 86400, dayOfWeek = (epoch / 86400 + 3) % 7;
  int alarmHour = alTms[String(dayOfWeek)]["h"], alarmMinute = alTms[String(dayOfWeek)]["m"];

  if ((int)alTms[String(dayOfWeek)]["a"] == 1 && alarmHour * 3600 + alarmMinute * 60 > timeOfDay)
  {
    alarmEpoch = epoch - timeOfDay + alarmHour * 3600 + alarmMinute * 60;
  }
  else if ((int)alTms[String((dayOfWeek + 1) % 7)]["a"] == 1)
  {
    alarmHour = alTms[String((dayOfWeek + 1) % 7)]["h"];
    alarmMinute = alTms[String((dayOfWeek + 1) % 7)]["m"];
    alarmEpoch = epoch - timeOfDay + alarmHour * 3600 + alarmMinute * 60 + 86400;
  }
  else
  {
    alarmEpoch = 0;
  }

  updateAlTmPending = false;

  digitalWrite(LED_BUILTIN, HIGH);
  Serial.print("#Updating Alarm Time succesful: ");
  Serial.println(alarmEpoch);
  Serial.println("------------------------------");
}

void connectToWiFi()
{
  digitalWrite(LED_BUILTIN, LOW);
  if (wifiStatus = WiFi.status() != WL_CONNECTED)
  {
    Serial.println("#Connecting to WiFi");
    while (wifiStatus != WL_CONNECTED)
    {
      wifiStatus = WiFi.begin(ssid, pass);
      Serial.println("#Connecting to WiFi");
    }
    Serial.println("#Connecting to WiFi succesful");
    Serial.println("------------------------------");
  }
  digitalWrite(LED_BUILTIN, HIGH);
}

void showTime(int epoch)
{
  int timeOfDay = epoch % 86400;
  int hours = timeOfDay / 3600;
  int minutes = (timeOfDay % 3600) / 60;
  for (int i = 0; i < 5; i++)
  {
    digitalWrite(hoursPins[i], (hours >> i) & 1);
  }
  for (int i = 0; i < 6; i++)
  {
    digitalWrite(minutesPins[i], (minutes >> i) & 1);
  }

  // digitalWrite(2, HIGH);
}

void hideTime()
{
  for (int i = 0; i < 5; i++)
  {
    digitalWrite(hoursPins[i], LOW);
  }
  for (int i = 0; i < 6; i++)
  {
    digitalWrite(minutesPins[i], LOW);
  }
}

void onShowTmInterrupt()
{
  int32_t ms = millis();
  if (idle)
  {
    wakeupInterrupt = true;
    showTimeBeg = ms;
    return;
  }
  getTimePending = true;
  updateAlTmPending = true;
}

void onPhotoResInterrupt()
{
  if (alarmRinging)
    return;
  idle = digitalRead(photoResPin);
}