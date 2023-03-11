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

P.S If you need to contact (placeholder) to retrieve Sseblo API key for TTS to work

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
