/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mbari.util;

import java.util.Collection;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Iterator;
import java.util.Date;
import java.util.ArrayList;

/**
 * <p><!--Insert summary here--></p><hr>
 *
 * @author  : $Author: kgomes $
 * @version : $Revision: 1.1 $
 * @testcase test.org.mbari.util.TestEmail
 *
 */
public class Email {
    public Collection getRecipients() { return recipients; }

    public void setRecipients(Collection recipients) {
        this.recipients = recipients;
    }

    public String getSender() { return sender; }

    public void setSender(String sender) { this.sender = sender; }

    public String getSmtpHost() { return smtpHost; }

    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public Collection getAttachments() { return attachments; }

    public void setAttachments(Collection attachments) {
        this.attachments = attachments;
    }

    public void send() throws AddressException, MessagingException {
        Properties props = System.getProperties();


            // -- Attaching to default Session, or we could start a new one --

        props.put("mail.smtp.host", smtpHost);
        Session session = Session.getDefaultInstance(props, null);

            // -- Create a new message --
        Message msg = new MimeMessage(session);

        Iterator iterator = recipients.iterator();
        String emailAddress = null;
        Address[] toAddresses = new Address[recipients.size()];
        int i = 0;
        while (iterator.hasNext()) {
            emailAddress = (String)iterator.next();
            toAddresses[i] = new InternetAddress(emailAddress);
            i++;
        }

            // -- Set the FROM and TO fields --
            //sender = (sender == null) ? ssdsProps.getProperty("mail.sender") : sender;
        msg.setFrom(new InternetAddress(sender));
        msg.setRecipients(Message.RecipientType.TO, toAddresses);

            // -- We could include CC recipients too --
            // if (cc != null)
            // msg.setRecipients(Message.RecipientType.CC
            // ,InternetAddress.parse(cc, false));

            // -- Set the subject and body text --
        subject = (subject == null) ? "No subject" : subject;
        msg.setSubject(subject);
        text = (text == null) ? "No Content" : text;
        msg.setText(text);

            // -- Set some other header information --
        msg.setHeader("X-Mailer", getClass().getName());
        msg.setSentDate(new Date());

            // -- Send the message --
        Transport.send(msg);


    }

    private Collection recipients = new ArrayList();
    private String sender;
    private String smtpHost;
    private String subject;
    private String text;
    private Collection attachments = new ArrayList();
}
