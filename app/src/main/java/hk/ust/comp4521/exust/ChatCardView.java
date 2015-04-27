package hk.ust.comp4521.exust;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import hk.ust.comp4521.exust.data.ApiHandler;
import hk.ust.comp4521.exust.data.ApiManager;
import hk.ust.comp4521.exust.data.CalendarEvent;
import hk.ust.comp4521.exust.data.ChatItem;
import hk.ust.comp4521.exust.data.Database;
import hk.ust.comp4521.exust.data.DatabaseLoad;
import hk.ust.comp4521.exust.data.Match2;
import hk.ust.comp4521.exust.data.ThreadItem;
import hk.ust.comp4521.exust.json.ApiResponseBase;

public class ChatCardView extends ThreadCardView {
	public ChatCardView(Context context) {
		super(context);
	}

	public ChatCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChatCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected ViewHolder createViewHolder() {
		return new ViewHolder();
	}

	@Override
	protected void initViews() {
		super.initViews();
		final ViewHolder vh = (ViewHolder) getTag();
		vh.bg = (View) findViewById(R.id.bg);
		vh.match_gp = (View) findViewById(R.id.match_gp);
		vh.join = (Button) findViewById(R.id.join);
		vh.match = (Button) findViewById(R.id.match);

		vh.join.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ChatCardView.this.getContext());
				ProgressBar bar = new ProgressBar(ChatCardView.this.getContext(), null,
						android.R.attr.progressBarStyleHorizontal);
				bar.setIndeterminate(true);
				builder.setCancelable(true).setTitle("Sending request to server").setView(bar);
				final AlertDialog dialog = builder.show();

                ArrayList<CalendarEvent> calendar2 = Database.getUser().getCalendar2();

                //Make arrays
                String CalStart[] = new String[calendar2.size()];
                String CalEnd[] = new String[calendar2.size()];
                String CalFreq[] = new String[calendar2.size()];
                for (int i=0; i < calendar2.size(); i++) {
                    CalStart[i] = calendar2.get(i).getFrom().toString();
                    CalEnd[i] = calendar2.get(i).getTo().toString();
                    CalFreq[i] = calendar2.get(i).getFreq();
                }

                ApiManager.joinMatch2(vh.chat.getMatchId(), CalStart, CalEnd, CalFreq, new ApiHandler<ApiResponseBase>() {

                    @Override
                    public void onSuccess(ApiResponseBase response) {
                        dialog.dismiss();
                        Toast.makeText(ChatCardView.this.getContext(), response.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        dialog.dismiss();
                        Toast.makeText(ChatCardView.this.getContext(), message,
                                Toast.LENGTH_LONG).show();
                    }

                });

			}

		});

        vh.match.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Database.getData("matches2", vh.chat.getMatchId(), Match2.class,
                        new DatabaseLoad<Map<String, Match2>>() {
                            @Override
                            public void load(Map<String, Match2> obj) {
                                if (obj == null)
                                    return;
                                ArrayList<Match2> threads = new ArrayList<Match2>();
                                threads.addAll(obj.values());
                                Toast.makeText(ChatCardView.this.getContext(), Integer.toString(threads.size()),
                                        Toast.LENGTH_LONG).show();

                                ArrayList<String> JointCalStart = new ArrayList<String>();
                                ArrayList<String> JointCalEnd = new ArrayList<String>();
                                ArrayList<String> JointCalFreq = new ArrayList<String>();
                                for (int i = 0; i < threads.size(); i++) {
                                    for (int j = 0; j < threads.get(i).getCalStart().length; j++) {
                                        JointCalStart.add( threads.get(i). getCalStart()[j] );
                                        JointCalEnd.add( threads.get(i). getCalEnd()[j] );
                                        JointCalFreq.add( threads.get(i). getCalFreq()[j] );
                                    }
                                }

                                MatchRecord slots = new MatchRecord();
                                slots.setParam(JointCalStart, JointCalEnd, JointCalFreq);
                                MainActivity main = (MainActivity) getContext();
                                main.gotoFragment(2, slots);

                            }
                        });
            }

        });

    }

	@Override
	public void setThread(ThreadItem thread) {
		ChatItem chat = (ChatItem) thread;
		ViewHolder vh = (ViewHolder) getTag();
		boolean isMine = chat.getAuthorId()
				.equals(Database.getUser().getITSC());
		Drawable bg = getResources().getDrawable(
				isMine ? R.drawable.chat_isuser : R.drawable.chat_notuser);
		vh.bg.setBackground(bg);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) vh.bg
				.getLayoutParams();
		params.gravity = isMine ? Gravity.RIGHT : Gravity.LEFT;
		vh.bg.setLayoutParams(params);

		LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) vh.title
				.getLayoutParams();
		params2.setMargins(isMine ? 0 : 10, 8, isMine ? 10 : 0, 0);
		params2.gravity = isMine ? Gravity.RIGHT : Gravity.LEFT;
		vh.title.setLayoutParams(params2);

		boolean isMatch = chat.getMatchId() != null;
		vh.match_gp.setVisibility(isMatch ? View.VISIBLE : View.GONE);
		
		vh.chat = chat;

		super.setThread(thread);
	}

	protected static class ViewHolder extends ThreadCardView.ViewHolder {
		public View bg, match_gp;
		public Button join, match;
		public ChatItem chat;
	}
}
