import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Handles interaction with Discord.
 */
public class DiscordMain extends ListenerAdapter {
	/**
	 * Run when the program is first launched.
	 *
	 * @param args terminal arguments
	 * @throws LoginException if there was an error logging in
	 * @throws IOException    if there was an error reading the bot token
	 */
	public static void main(String[] args) throws LoginException, IOException {
		URL botTokenUrl = DiscordMain.class.getResource("/bot.secret");
		String s = Files.readString(Path.of(botTokenUrl.getPath()));
		JDABuilder.createDefault(s).addEventListeners(new DiscordMain()).build();
	}
	
	/**
	 * Handles when a {@link User} sends a
	 * {@link Message} in a {@link Guild}.
	 *
	 * @param event the event
	 */
	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		
		Message message = event.getMessage();
		
		if (message.getAttachments().isEmpty()) return;
		if (!message.getContentRaw().toLowerCase(Locale.ROOT).trim().equals("analyze my code")) return;
		
		for (Message.Attachment attachment : message.getAttachments()) {
			if (attachment.getSize() > 100000) {
				message.getChannel().sendMessage(attachment.getFileName() + " is too big.").queue();
				return;
			}
			File fileToAnalyze = new File("analyze", attachment.getFileName());
			attachment.downloadToFile(fileToAnalyze).thenAccept(
					file -> {
						try {
							message.getChannel().sendFile(Analyzer.buildChart(Analyzer.analyzeCodeFile(fileToAnalyze))).queue();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			);
		}
	}
}
