function validateType(variable, name, allowedTypes) {
    for (const allowedType of allowedTypes) {
        if (typeof allowedType == "string") {
            if (typeof variable == allowedType) {
                return true;
            }
        } else {
            if (variable instanceof allowedType) {
                return true;
            }
        }
    }

    throw `${name} must be one of the following types: ${allowedTypes.join()}`;
}

function validate(variable, schema) {
    const {
        allowedTypes,
        allowedArrayTypes,
        nullable,
        name
    } = schema;

    if ((variable == null) || (variable == undefined)) {
        if (nullable) {
            return true;
        } else {
            throw `${name} cannot be null.`;
        }
    } else {
        if (Array.isArray(variable) && allowedTypes.includes(Array)) {
            // Check the nested types
            let idx = 0;
            for (const value of variable) {
                validateType(value, `${name}[${idx}]`, allowedArrayTypes);
                idx++;
            }

            return true;
        } else {
            return validateType(variable, name, allowedTypes);
        }
    }
}

export {
    validate
};