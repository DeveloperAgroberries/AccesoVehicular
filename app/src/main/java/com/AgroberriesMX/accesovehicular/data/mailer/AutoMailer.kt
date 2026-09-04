package com.AgroberriesMX.accesovehicular.data.mailer

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.*
import javax.mail.internet.*

@Singleton
class AutoMailer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun sendEmailWithAttachmentInBackground(
        toEmail: String,
        subject: String,
        bodyText: String,
        attachmentContent: String,
        fileName: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (toEmail.isNotBlank()) {
                val host = "smtp.gmail.com"
                val port = "587"
                val fromEmail = "portal.agroberries@gmail.com"
                val password = "esno yahh evsf mazo"

                val properties = Properties().apply {
                    put("mail.smtp.host", host)
                    put("mail.smtp.port", port)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(fromEmail, password)
                    }
                })

                try {
                    val tempFile = File(context.cacheDir, fileName)
                    tempFile.writeText(attachmentContent)

                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress(fromEmail))
                        setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                        this.subject = subject

                        val multipart = MimeMultipart().apply {
                            addBodyPart(MimeBodyPart().apply { setText(bodyText) })
                            addBodyPart(MimeBodyPart().apply { attachFile(tempFile) })
                        }
                        setContent(multipart)
                    }

                    Transport.send(message)
                    Log.d("AutoMailer", "Logs enviados exitosamente a: $toEmail")

                    if (tempFile.exists()) tempFile.delete()

                } catch (e: Exception) {
                    Log.e("AutoMailer", "Error al enviar correo de logs: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}