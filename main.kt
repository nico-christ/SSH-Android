import com.jcraft.jsch.Channel
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.util.Properties

class SSHClient {

    private var session: Session? = null

    fun connect(host: String, username: String, password: String): Boolean {
        return try {
            val jsch = JSch()
            session = jsch.getSession(username, host, 22)
            session?.setPassword(password)

            // For security, ignore host key checking during the development phase
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session?.setConfig(config)

            session?.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disconnect() {
        session?.disconnect()
    }

    fun isConnected(): Boolean {
        return session?.isConnected ?: false
    }

    fun executeCommand(command: String): String {
        if (!isConnected()) {
            throw IllegalStateException("Not connected to an SSH server")
        }

        return try {
            val channel: Channel = session?.openChannel("exec") ?: throw Exception("Failed to open channel")
            (channel as? Channel.Exec)?.setCommand(command)

            val inputStream = channel.inputStream
            val output = StringBuilder()

            channel.connect()

            // Read the command output
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                output.append(String(buffer, 0, bytesRead))
            }

            channel.disconnect()
            output.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

fun main() {
    val sshClient = SSHClient()

    val host = "your-ssh-server-host" //TODO Change later!
    val username = "your-ssh-username" //TODO Change later!
    val password = "your-ssh-password" //TODO Change later!

    if (sshClient.connect(host, username, password)) {
        println("Connected to $host")
        
        // Execute a command (replace with your desired command)
        val commandOutput = sshClient.executeCommand("ls -l")
        println("Command Output:\n$commandOutput")

        // Disconnect when done
        sshClient.disconnect()
    } else {
        println("Failed to connect to $host")
    }
}
