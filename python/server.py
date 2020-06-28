from flask import Flask, request, jsonify

app = Flask(__name__)

confirmed = {}
deaths = {}
recovered = {}
sick = {}


def load_data(type, src):
    if type == 'confirmed':
        return confirmed.setdefault(src, [])
    elif type == 'deaths':
        return deaths.setdefault(src, [])
    elif type == 'recovered':
        return recovered.setdefault(src, [])
    elif "sick":
        return sick.setdefault(src, [])
    else:
        raise ValueError(type + " is invalid")


@app.route("/api/<type>/<src>")
def get_data(type, src):
    return jsonify(load_data(type, src))


@app.route("/api/<type>/<src>", methods=['POST'])
def post_data(type, src):
    loaded = load_data(type, src)
    loaded.append(request.json)
    return '', 201


@app.route("/api/<type>/<src>", methods=['DELETE'])
def delete_data(type, src):
    loaded = load_data(type, src)
    loaded.remove(request.json)
    return '', 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
