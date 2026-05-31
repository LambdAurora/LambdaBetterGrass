package net.neoforged.neoforge.client.model.data;

public final class ModelData {
	public <T> T get(ModelProperty<T> property) {
		throw new AssertionError("Shim only.");
	}

	public Builder derive() {
		throw new AssertionError("Shim only.");
	}

	public static final class Builder {
		public <T> Builder with(ModelProperty<T> property, T value) {
			throw new AssertionError("Shim only.");
		}

		public ModelData build() {
			throw new AssertionError("Shim only.");
		}
	}
}
