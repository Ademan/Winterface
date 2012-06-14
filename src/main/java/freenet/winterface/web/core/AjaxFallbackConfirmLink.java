package freenet.winterface.web.core;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.model.IModel;

import freenet.winterface.web.markup.ConfirmPanel;

/**
 * An {@link AjaxFallbackCssButton} which renders a {@link ConfirmPanel} when
 * clicked.
 * 
 * @author pausb
 * @see AjaxFallbackButton
 */
@SuppressWarnings("serial")
public abstract class AjaxFallbackConfirmLink extends AjaxFallbackCssButton {

	/**
	 * {@link Component} to render {@link ConfirmPanel}
	 */
	private Component parent;

	/**
	 * Default message for {@link ConfirmPanel}
	 */
	private String msg = "Are you sure?";

	/**
	 * Constructs
	 * 
	 * @param id
	 *            Wicket ID of link
	 * @param parent
	 *            Component which is going to house {@link ConfirmPanel}
	 */
	public AjaxFallbackConfirmLink(String id, Component parent) {
		this(id, null, parent);
	}

	/**
	 * Constructs
	 * 
	 * @param id
	 *            Wicket ID of link
	 * @param model
	 *            {@link IModel} for link
	 * @param parent
	 *            Component which is going to house {@link ConfirmPanel}
	 */
	public AjaxFallbackConfirmLink(String id, IModel<String> model, Component parent) {
		super(id, model);
		this.parent = parent;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		ConfirmPanel cp = new ConfirmPanel(parent, msg) {
			@Override
			protected void onOk(AjaxRequestTarget target) {
				onConfirm(target);
			}
		};
		parent.replaceWith(cp);
		if (target != null) {
			target.add(cp);
		}
	}

	/**
	 * Sets confirm message
	 * 
	 * @param msg
	 *            confirm message
	 */
	public void setMessgae(String msg) {
		this.msg = msg;
	}

	/**
	 * Callback when confirm button is pressed by {@link ConfirmPanel}
	 * 
	 * @param target
	 *            {@code null} if JS disabled
	 */
	public abstract void onConfirm(AjaxRequestTarget target);

}