
![https://wakatime.com/badge/user/f40d191c-28ee-465f-b951-6d8cae19f30f/project/5b9872dd-7c16-455e-8252-a1e7fcd8280d.svg](https://wakatime.com/badge/user/f40d191c-28ee-465f-b951-6d8cae19f30f/project/5b9872dd-7c16-455e-8252-a1e7fcd8280d.svg)
![https://deepsource.io/gh/Andorvini/DingusPlayer/?ref=repository-badge](https://deepsource.io/gh/Andorvini/DingusPlayer.svg/?label=active+issues&show_trend=true&token=lD2WmPBYq7_ih4hBj6R10OY8)
<h1>Dingus Player</h1>

<h2>Discord Music Bot</h2>

<ins>**Supported Formats:**</ins>
<ul>
    <li>MP3</li>
    <li>WAV</li>
    <li>MP4</li>
    <li>FLAC</li>
    <li>OGG</li>
</ul>

<ins>**Supported Sources:**</ins>
<ul>
    <li>Youtube</li>
    <li>Youtube Streams</li>
    <li>Direct URL to all mentioned formats</li>
    <li>Bandcamp (I don't know what is it)</li>
</ul>

<ins>**Features:**</ins>
<ul>
    <li>Queue</li>
    <li>Search over Youtube</li>
    <li>Custom greetings</li>
    <li>Dev tools</li>
    <li>Text-To-Speech with a lot of voices (with condition)</li>
    <li>Picking random user from a voice channel</li>
</ul>

P.S You need to contact `me@vprw.ru` to retrieve Sseblo API key for TTS to work

<h2>Self-Hosting</h2>

<h3>Docker</h3>

| Environment Variable Name |                                            Purpose                                            |
|---------------------------|:---------------------------------------------------------------------------------------------:|
| DP_DISCORD_TOKEN          |                                       Discord API token                                       |
| DP_SOSANIE_TTS_ENABLED    |                                   Defines if TTS is enabled                                   |
| DP_SOSANIE_API_KEY        |                   Sseblo API key, needed only if the item is on top is true                   |
| DP_YOUTUBE_API_KEY        |                                        Youtube API key                                        |
| DP_YOUTUBE_LOGIN          | Login for Youtube account, i suggest you creating a new account that will be used only by bot |
| DP_YOUTUBE_PASSWORD       |                               Youtube password for bot account                                |

<h3>Not Docker</h3>

<h3>Dev Tools Queries ( /dev )</h3>

| Query                  |                               Description                               |
|------------------------|:-----------------------------------------------------------------------:|
| servers                |                    Shows all servers that have a bot                    | 
| hashmaps               |            Shows currently existing Hashmaps for this server            |
| sortVoiceChannels      |                    Reversing the voice channel order                    |
| getFromBackup          | Will create all the voice channels that will be defined in `backup.txt` |
| clearVoices            |              Deletes all the voice channels in the server               |


