package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.terminal.data.data.TripInfo;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 22.06.12
 * Time: 17:40
 */
public class CommitHands
{
    private ByteArrayOutputStream headerBody;
    private ByteArrayOutputStream entityBody;
    private final byte[] postSeparator = "&".getBytes();
    private final byte[] separator = "\r\n".getBytes();
    Logger log = Logger.getLogger(getClass().getName());

    public CommitHands() throws IOException
    {
        entityBody = new ByteArrayOutputStream();
        headerBody = new ByteArrayOutputStream();
    }

    /**
     * this method opens socket and send data there. Then read answer.
     *
     * @throws java.io.IOException
     */
    public TripInfo send() throws IOException, ServerApiException
    {
        Socket socket = null;
        OutputStream os;
        try
        {
            socket = new Socket("api.timetable.asia", 80);
            os = socket.getOutputStream();
            os.write(this.headerBody.toByteArray());
            os.write(this.entityBody.toByteArray());
            os.flush();

            BufferedReader is = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            String line;
            StringBuilder answer = new StringBuilder();
            while ((line = is.readLine()) != null)
            {
                answer.append(line);
            }
            log.warning(answer.toString());
            try
            {
                int breakStart = answer.toString().indexOf("{");
                int breakEnd = answer.toString().lastIndexOf("}");
                if (-1 != breakStart && -1 != breakEnd)
                {
                    if (breakEnd == answer.length() - 1)
                    {
                        String jsonAnswer = answer.substring(breakStart, breakEnd + 1);
                        JSONObject answerObj = new JSONObject(jsonAnswer);
                        return CommitRequest.parseUnswer(answerObj);
                    }
                }
                else
                {
                    throw new ServerApiException();
                }
            } catch (Exception e)
            {
                throw new ServerApiException(e);
            }

        } finally
        {
            try
            {
                socket.close();
            } catch (Exception e)
            {
            }
        }
        throw new ServerApiException();
    }

    /**
     * This method makes entity body of request. Both string and image.
     *
     * @throws IOException
     */
    public void makeEntityBody(String commit) throws IOException
    {
        entityBody.write("cmd=commit.json".getBytes());
        entityBody.write(postSeparator);
        entityBody.write("q=".getBytes());
        entityBody.write(commit.getBytes());
        entityBody.write(postSeparator);
    }

    /**
     * This method makes header of request and adds all needed data to it.
     *
     * @throws IOException
     */
    public void makeHeader() throws IOException
    {
        this.headerBody.write("POST ".getBytes());
        this.headerBody.write("/".getBytes());
        this.headerBody.write(" HTTP/1.0".getBytes());
        this.headerBody.write(this.separator);
        this.headerBody.write("HOST: ".getBytes());
        this.headerBody.write("api.timetable.asia".getBytes());
        this.headerBody.write(this.separator);
        this.headerBody.write("Content-Type: application/x-www-form-urlencoded".getBytes());
        this.headerBody.write(this.separator);
        this.headerBody.write("Content-Length: ".getBytes());
        this.headerBody.write(String.valueOf(this.entityBody.size()).getBytes());
        this.headerBody.write(this.separator);
        this.headerBody.write(this.separator);
    }
}
