package org.kevin.trello_v2.email.service.impl

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {
    fun sendEmail(to: String, subject: String, body: String) {
        val message = SimpleMailMessage()
//        message.setTo(to) TODO: during development, use a fixed email address
        message.setTo("doddlefeng@gmail.com")
        message.subject = subject
        message.text = body
        message.from = "hello@demomailtrap.co"

        mailSender.send(message)
    }
}