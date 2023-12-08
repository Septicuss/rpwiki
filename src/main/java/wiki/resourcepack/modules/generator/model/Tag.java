package wiki.resourcepack.modules.generator.model;

public final record Tag(String id, String title, String backgroundColor, String textColor) {

	public static Builder tag(String id) {
		return new Builder(id);
	}
	
	public static class Builder {
		
		public static final String DEFAULT_BACKGROUND_COLOR = "#ffffff";
		public static final String DEFAULT_TEXT_COLOR = "#000000";
		
		private String id;
		private String title;
		private String backgroundColor;
		private String textColor;
		
		private Builder(String id) {
			this.id = id;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder backgroundColor(String backgroundColor) {
			this.backgroundColor = backgroundColor;
			return this;
		}
		
		public Builder textColor(String textColor) {
			this.textColor = textColor;
			return this;
		}
		
		public Tag build() {
			
			if (this.title == null)
				this.title = this.id;
			
			if (this.backgroundColor == null)
				this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
			
			if (this.textColor == null)
				this.textColor = DEFAULT_TEXT_COLOR;
			
			return new Tag(this.id, this.title, this.backgroundColor, this.textColor);
			
		}
		
	}
	
}
